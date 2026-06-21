package com.ridehailing.rideservice.client.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationApiResponse<T> {
    private boolean success;
    private String  message;
    private T       data;
    private Instant timestamp;
}
