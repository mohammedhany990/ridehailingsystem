package com.ridehailing.mapservice.dto;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class OsrmResponse {

    private List<Route> routes;

    @Data
    public static class Route {

        private double distance;

        private double duration;
    }
}