package com.ridehailing.locationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridehailing.locationservice.dto.DriverSnapshot;
import com.ridehailing.locationservice.entity.DriverLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLocationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String LOCATION_PREFIX = "driver:location:";
    private static final String SNAPSHOT_PREFIX = "driver:snapshot:";
    private static final String GEO_KEY = "drivers:geo";

    private static final Duration TTL = Duration.ofMinutes(5);

    public void save(DriverLocation location) {
        if (location == null || location.getDriverId() == null) return;

        String driverId = location.getDriverId();

        try {
            redisTemplate.opsForValue().set(
                    locationKey(driverId),
                    location,
                    TTL
            );

            if (location.getLocation() != null) {
                redisTemplate.opsForGeo().add(
                        GEO_KEY,
                        new Point(location.getLongitude(), location.getLatitude()),
                        driverId
                );
            }

            saveSnapshot(location);

            log.debug("Redis updated for driver={}", driverId);

        } catch (Exception e) {
            log.error("Redis save failed for driver={}", driverId, e);
        }
    }

    public void saveSnapshot(DriverLocation location) {

        if (location == null) return;

        try {
            DriverSnapshot snapshot = DriverSnapshot.builder()
                    .driverId(location.getDriverId())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .speedKmh(location.getSpeedKmh())
                    .bearing(location.getBearing())
                    .updatedAt(location.getUpdatedAt())
                    .build();

            Map<String, Object> map =
                    objectMapper.convertValue(snapshot, Map.class);

            String key = snapshotKey(location.getDriverId());

            redisTemplate.opsForHash().putAll(key, map);
            redisTemplate.expire(key, TTL);

        } catch (Exception e) {
            log.error("Snapshot save failed for driver={}", location.getDriverId(), e);
        }
    }

    public DriverSnapshot getSnapshot(String driverId) {

        try {
            Map<Object, Object> map =
                    redisTemplate.opsForHash().entries(snapshotKey(driverId));

            if (map == null || map.isEmpty()) return null;

            return objectMapper.convertValue(map, DriverSnapshot.class);

        } catch (Exception e) {
            log.error("Snapshot read failed driver={}", driverId, e);
            return null;
        }
    }

    public List<DriverSnapshot> getSnapshots(List<String> driverIds) {

        if (driverIds == null || driverIds.isEmpty()) return List.of();

        return driverIds.parallelStream()
                .map(this::getSnapshot)
                .filter(Objects::nonNull)
                .toList();
    }

    public GeoResults<RedisGeoCommands.GeoLocation<Object>> findNearbyGeo(
            Circle circle,
            int limit
    ) {

        try {
            RedisGeoCommands.GeoRadiusCommandArgs args =
                    RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                            .includeDistance()
                            .includeCoordinates()
                            .sortAscending()
                            .limit(limit);

            GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                    redisTemplate.opsForGeo().radius(GEO_KEY, circle, args);

            if (results == null || results.getContent() == null) {
                return null;
            }

            return results;

        } catch (Exception e) {
            log.error("Geo search failed", e);
            return null;
        }
    }

    public DriverLocation get(String driverId) {
        try {
            Object value = redisTemplate.opsForValue().get(locationKey(driverId));
            if (value == null) return null;

            return objectMapper.convertValue(value, DriverLocation.class);

        } catch (Exception e) {
            log.error("Redis full get failed driver={}", driverId, e);
            return null;
        }
    }

    public void delete(String driverId) {

        try {
            redisTemplate.delete(locationKey(driverId));
            redisTemplate.delete(snapshotKey(driverId));
            redisTemplate.opsForGeo().remove(GEO_KEY, driverId);

        } catch (Exception e) {
            log.error("Redis delete failed driver={}", driverId, e);
        }
    }

    private String locationKey(String driverId) {
        return LOCATION_PREFIX + driverId;
    }

    private String snapshotKey(String driverId) {
        return SNAPSHOT_PREFIX + driverId;
    }
}