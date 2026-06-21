package com.ridehailing.userservice.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {

    private UUID id;

    private String plateNumber;

    private String make;

    private String model;

    private String color;

    private Integer year;

    private LocalDateTime createdAt;
}