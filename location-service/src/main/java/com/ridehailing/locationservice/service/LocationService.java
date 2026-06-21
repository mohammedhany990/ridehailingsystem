package com.ridehailing.locationservice.service;

import com.ridehailing.locationservice.common.ApiResponse;
import com.ridehailing.locationservice.dto.*;
import com.ridehailing.locationservice.entity.DriverLocation;
import com.ridehailing.locationservice.entity.LocationHistory;
import com.ridehailing.locationservice.exception.DriverNotFoundException;
import com.ridehailing.locationservice.mapper.LocationMapper;
import com.ridehailing.locationservice.repository.DriverLocationRepository;
import com.ridehailing.locationservice.repository.LocationHistoryRepository;
import com.ridehailing.locationservice.util.GeoUtils;
import com.ridehailing.locationservice.websocket.LocationBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.GeoResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final DriverLocationRepository locationRepository;
    private final LocationHistoryRepository historyRepository;
    private final RedisLocationService redisLocationService;
    private final LocationMapper locationMapper;
    private final LocationBroadcaster locationBroadcaster;

    public ApiResponse<LocationResponse> updateLocation(UpdateLocationRequest request) {

        String driverId = request.getDriverId();

        GeoUtils.validate(request.getLatitude(), request.getLongitude());

        log.info("Updating location driverId={} lat={} lng={}",
                driverId, request.getLatitude(), request.getLongitude());

        DriverLocation location = locationRepository
                .findByDriverId(driverId)
                .orElse(null);

        Double speedKmh = request.getSpeedKmh();
        Double bearing = request.getBearing();

        if (location == null) {

            location = DriverLocation.create(
                    driverId,
                    request.getLatitude(),
                    request.getLongitude()
            );

        } else {

            if (speedKmh == null && location.getLocation() != null) {

                double distKm = GeoUtils.distanceKm(
                        location.getLatitude(),
                        location.getLongitude(),
                        request.getLatitude(),
                        request.getLongitude()
                );

                long elapsed = Math.max(1,
                        Instant.now().getEpochSecond()
                                - Optional.ofNullable(location.getUpdatedAt())
                                .orElse(Instant.now())
                                .getEpochSecond()
                );

                speedKmh = GeoUtils.speedKmh(distKm, elapsed);
            }

            if (bearing == null && location.getLocation() != null) {
                bearing = GeoUtils.bearing(
                        location.getLatitude(),
                        location.getLongitude(),
                        request.getLatitude(),
                        request.getLongitude()
                );
            }

            location.updateLocation(
                    request.getLatitude(),
                    request.getLongitude(),
                    speedKmh,
                    bearing
            );
        }

        locationRepository.save(location);

        try {
            historyRepository.save(
                    LocationHistory.from(
                            driverId,
                            request.getLatitude(),
                            request.getLongitude(),
                            speedKmh,
                            bearing
                    )
            );
        } catch (Exception e) {
            log.error("Failed saving history driver={}", driverId, e);
        }

        try {
            redisLocationService.save(location);
        } catch (Exception e) {
            log.error("Redis update failed driver={}", driverId, e);
        }

        try {
            LocationResponse response = locationMapper.toResponse(location);
            locationBroadcaster.broadcast(response);

            return ApiResponse.success("Location updated", response);

        } catch (Exception e) {
            log.error("Broadcast failed driver={}", driverId, e);
            return ApiResponse.success("Location updated but broadcast failed", null);
        }
    }

    public ApiResponse<LocationResponse> getLocation(String driverId) {

        log.info("Fetching location driverId={}", driverId);

        DriverSnapshot snapshot = redisLocationService.getSnapshot(driverId);

        if (snapshot != null) {
            return ApiResponse.success(
                    "Location fetched from cache",
                    locationMapper.toResponse(snapshot)
            );
        }
        DriverLocation location = locationRepository
                .findByDriverId(driverId)
                .orElseThrow(() ->
                        new DriverNotFoundException("Driver not found: " + driverId)
                );

        try {
            redisLocationService.save(location);
        } catch (Exception e) {
            log.error("Redis warm-up failed driver={}", driverId, e);
        }

        return ApiResponse.success(
                "Location fetched from database",
                locationMapper.toResponse(location)
        );
    }

    public ApiResponse<List<NearbyDriversResponse>> findNearbyDrivers(NearbyDriversRequest request) {

        Circle circle = new Circle(
                new Point(request.getLongitude(), request.getLatitude()),
                new Distance(request.getRadiusKm(), Metrics.KILOMETERS)
        );

        GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResults =
                redisLocationService.findNearbyGeo(circle, request.getLimit());

        if (geoResults == null || geoResults.getContent().isEmpty()) {
            return ApiResponse.success("Nearby drivers fetched: 0", List.of());
        }

        List<String> driverIds = geoResults.getContent()
                .stream()
                .map(r -> r.getContent().getName().toString())
                .toList();

        Map<String, DriverSnapshot> snapshotMap = redisLocationService.getSnapshots(driverIds)
                .stream()
                .collect(Collectors.toMap(DriverSnapshot::getDriverId, s -> s));

        List<NearbyDriversResponse> results = new ArrayList<>();

        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> geoResult : geoResults.getContent()) {

            String driverId = geoResult.getContent().getName().toString();

            DriverSnapshot snapshot = snapshotMap.get(driverId);
            if (snapshot == null) continue;

            Double distanceKm = request.isIncludeDistance()
                    ? geoResult.getDistance().getValue()
                    : null;

            results.add(locationMapper.toNearbyResponse(snapshot, distanceKm));
        }

        return ApiResponse.success(
                "Nearby drivers fetched: " + results.size(),
                results
        );
    }

    public ApiResponse<List<LocationHistoryResponse>> getHistory(String driverId) {

        List<LocationHistoryResponse> history =
                historyRepository.findTop50ByDriverIdOrderByCreatedAtDesc(driverId)
                        .stream()
                        .map(locationMapper::toHistoryResponse)
                        .toList();

        return ApiResponse.success("History fetched", history);
    }

    public ApiResponse<Void> heartbeat(String driverId) {

        DriverLocation location = locationRepository
                .findByDriverId(driverId)
                .orElseThrow(() ->
                        new DriverNotFoundException("Driver not found: " + driverId)
                );

        location.heartbeat();
        locationRepository.save(location);

        try {
            redisLocationService.save(location);
        } catch (Exception e) {
            log.error("Heartbeat Redis update failed driver={}", driverId, e);
        }

        log.debug("Heartbeat updated driverId={}", driverId);

        return ApiResponse.success("Heartbeat updated", null);
    }

    public ApiResponse<Void> removeDriverLocation(String driverId) {

        locationRepository.findByDriverId(driverId)
                .ifPresent(locationRepository::delete);

        try {
            redisLocationService.delete(driverId);
        } catch (Exception e) {
            log.error("Redis delete failed driver={}", driverId, e);
        }

        log.info("Driver {} removed", driverId);

        return ApiResponse.success("Driver removed", null);
    }
}