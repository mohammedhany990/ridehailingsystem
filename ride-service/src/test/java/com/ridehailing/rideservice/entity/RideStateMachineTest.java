package com.ridehailing.rideservice.entity;

import com.ridehailing.rideservice.exception.InvalidRideStateException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideStateMachineTest {

    private Ride newRide() {
        return Ride.builder()
                .id(UUID.randomUUID())
                .riderId(UUID.randomUUID())
                .pickupLat(30.0).pickupLng(31.0)
                .dropoffLat(30.1).dropoffLng(31.1)
                .build();
    }

    @Test
    void happyPathFlowsThroughAllStates() {
        Ride ride = newRide();
        assertThat(ride.getStatus()).isEqualTo(RideStatus.REQUESTED);

        ride.accept(UUID.randomUUID());
        assertThat(ride.getStatus()).isEqualTo(RideStatus.ACCEPTED);
        assertThat(ride.getAcceptedAt()).isNotNull();

        ride.start();
        assertThat(ride.getStatus()).isEqualTo(RideStatus.IN_PROGRESS);
        assertThat(ride.getStartedAt()).isNotNull();

        ride.complete();
        assertThat(ride.getStatus()).isEqualTo(RideStatus.COMPLETED);
        assertThat(ride.getCompletedAt()).isNotNull();
    }

    @Test
    void cannotStartBeforeAccept() {
        Ride ride = newRide();
        assertThatThrownBy(ride::start).isInstanceOf(InvalidRideStateException.class);
    }

    @Test
    void cannotCompleteBeforeStart() {
        Ride ride = newRide();
        ride.accept(UUID.randomUUID());
        assertThatThrownBy(ride::complete).isInstanceOf(InvalidRideStateException.class);
    }

    @Test
    void cannotAcceptTwice() {
        Ride ride = newRide();
        ride.accept(UUID.randomUUID());
        assertThatThrownBy(() -> ride.accept(UUID.randomUUID()))
                .isInstanceOf(InvalidRideStateException.class);
    }

    @Test
    void cancelRecordsReasonAndBlocksFurtherTransitions() {
        Ride ride = newRide();
        ride.cancel("RIDER", "changed my mind");

        assertThat(ride.getStatus()).isEqualTo(RideStatus.CANCELLED);
        assertThat(ride.getCancellation().getCancelledBy()).isEqualTo("RIDER");
        assertThat(ride.getCancellation().getReason()).isEqualTo("changed my mind");
        assertThatThrownBy(() -> ride.accept(UUID.randomUUID()))
                .isInstanceOf(InvalidRideStateException.class);
    }

    @Test
    void completedRideCannotBeCancelled() {
        Ride ride = newRide();
        ride.accept(UUID.randomUUID());
        ride.start();
        ride.complete();
        assertThatThrownBy(() -> ride.cancel("RIDER", "too late"))
                .isInstanceOf(InvalidRideStateException.class);
    }
}
