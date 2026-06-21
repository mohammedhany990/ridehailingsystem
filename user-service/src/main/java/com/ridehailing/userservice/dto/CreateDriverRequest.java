package com.ridehailing.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDriverRequest {

    @NotBlank(message = "License number is required")
    private String licenseNumber;
}
