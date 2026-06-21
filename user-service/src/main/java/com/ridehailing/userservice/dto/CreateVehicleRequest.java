package com.ridehailing.userservice.dto;


import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleRequest {

    @NotBlank(message = "Plate number is required")
    @Size(max = 20, message = "Plate number cannot exceed 20 characters")
    private String plateNumber;

    @NotBlank(message = "Vehicle make is required")
    @Size(max = 50, message = "Vehicle make cannot exceed 50 characters")
    private String make;

    @NotBlank(message = "Vehicle model is required")
    @Size(max = 50, message = "Vehicle model cannot exceed 50 characters")
    private String model;

    @NotBlank(message = "Vehicle color is required")
    @Size(max = 30, message = "Vehicle color cannot exceed 30 characters")
    private String color;

    @NotNull(message = "Vehicle year is required")
    @Min(value = 1980, message = "Vehicle year is invalid")
    @Max(value = 2100, message = "Vehicle year is invalid")
    private Integer year;
}