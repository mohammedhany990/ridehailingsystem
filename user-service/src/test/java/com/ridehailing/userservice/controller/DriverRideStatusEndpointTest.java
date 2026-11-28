package com.ridehailing.userservice.controller;

import com.ridehailing.userservice.common.ApiResponse;
import com.ridehailing.userservice.service.DriverProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverRideStatusEndpointTest {

    @Mock
    private DriverProfileService driverProfileService;

    @InjectMocks
    private DriverProfileController controller;

    @Test
    void startRideForDriverDelegatesToService() {
        UUID userId = UUID.randomUUID();
        ApiResponse<String> ok = ApiResponse.<String>builder()
                .success(true).message("Ride started").data("ON_RIDE").build();
        when(driverProfileService.startRide(userId)).thenReturn(ok);

        var response = controller.startRideForDriver(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ON_RIDE", response.getBody().getData());
        verify(driverProfileService).startRide(userId);
    }

    @Test
    void finishRideForDriverDelegatesToService() {
        UUID userId = UUID.randomUUID();
        ApiResponse<String> ok = ApiResponse.<String>builder()
                .success(true).message("Ride finished").data("AVAILABLE").build();
        when(driverProfileService.finishRide(userId)).thenReturn(ok);

        var response = controller.finishRideForDriver(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("AVAILABLE", response.getBody().getData());
        verify(driverProfileService).finishRide(userId);
    }
}
