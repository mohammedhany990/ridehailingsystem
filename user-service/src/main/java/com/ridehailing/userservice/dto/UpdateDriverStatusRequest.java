package com.ridehailing.userservice.dto;

import com.ridehailing.userservice.entity.DriverStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateDriverStatusRequest {

    @NotNull(message = "Status is required")
    private DriverStatus status;
}