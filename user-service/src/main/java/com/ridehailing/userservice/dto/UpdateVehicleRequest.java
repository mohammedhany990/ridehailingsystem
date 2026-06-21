package com.ridehailing.userservice.dto;


import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehicleRequest {

    @NotBlank(message = "Vehicle make is required")
    @Size(max = 50)
    private String make;

    @NotBlank(message = "Vehicle model is required")
    @Size(max = 50)
    private String model;

    @NotBlank(message = "Vehicle color is required")
    @Size(max = 30)
    private String color;

    @NotNull(message = "Vehicle year is required")
    @Min(1980)
    @Max(2100)
    private Integer year;
}