package com.ridehailing.rideservice.client.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserApiResponse<T> {
    private T       data;
    private String  message;
    private boolean success;
}
