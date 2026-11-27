package com.ridehailing.rideservice.mapper;

import com.ridehailing.rideservice.dto.CancellationResponse;
import com.ridehailing.rideservice.dto.FareResponse;
import com.ridehailing.rideservice.dto.RideResponse;
import com.ridehailing.rideservice.entity.Fare;
import com.ridehailing.rideservice.entity.Ride;
import com.ridehailing.rideservice.entity.RideCancellation;
import org.springframework.stereotype.Component;

@Component
public class RideMapper {

    public RideResponse toResponse(Ride ride) {
        return RideResponse.builder()
                .id(ride.getId())
                .riderId(ride.getRiderId())
                .driverId(ride.getDriverId())
                .status(ride.getStatus())
                .vehicleType(ride.getVehicleType())
                .pickupLat(ride.getPickupLat())
                .pickupLng(ride.getPickupLng())
                .dropoffLat(ride.getDropoffLat())
                .dropoffLng(ride.getDropoffLng())
                .requestedAt(ride.getRequestedAt())
                .acceptedAt(ride.getAcceptedAt())
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt())
                .fare(toFareResponse(ride.getFare()))
                .cancellation(toCancellationResponse(ride.getCancellation()))
                .build();
    }

    private FareResponse toFareResponse(Fare fare) {
        if (fare == null) return null;
        return FareResponse.builder()
                .id(fare.getId())
                .baseFare(fare.getBaseFare())
                .distanceKm(fare.getDistanceKm())
                .surgeMultiplier(fare.getSurgeMultiplier())
                .totalAmount(fare.getTotalAmount())
                .currency(fare.getCurrency())
                .calculatedAt(fare.getCalculatedAt())
                .build();
    }

    private CancellationResponse toCancellationResponse(RideCancellation cancellation) {
        if (cancellation == null) return null;
        return CancellationResponse.builder()
                .id(cancellation.getId())
                .cancelledBy(cancellation.getCancelledBy())
                .reason(cancellation.getReason())
                .cancelledAt(cancellation.getCancelledAt())
                .cancellationFee(cancellation.getCancellationFee())
                .build();
    }
}
