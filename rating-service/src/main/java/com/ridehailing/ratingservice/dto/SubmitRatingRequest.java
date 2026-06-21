package com.ridehailing.ratingservice.dto;

import com.ridehailing.ratingservice.entity.PartyType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmitRatingRequest {

    @NotNull
    private UUID rideId;

    @NotNull
    private UUID rateeId;

    @NotNull
    private PartyType rateeType;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 500)
    private String comment;
}