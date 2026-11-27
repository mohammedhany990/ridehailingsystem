package com.ridehailing.rideservice.dto;

import com.ridehailing.rideservice.entity.RideStatus;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrailResponse {

    private UUID rideId;
    private RideStatus status;
    private int pointCount;
    private List<BreadcrumbPoint> points;
}
