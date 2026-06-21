package com.ridehailing.rideservice.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "ID must not be null")
    private Long id;

    @NotBlank(message = "Email must not be blank")
    private String email;

    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotBlank(message = "Message must not be blank")
    private String message;

    @NotNull(message = "Notification type must not be null")
    private String type;
}