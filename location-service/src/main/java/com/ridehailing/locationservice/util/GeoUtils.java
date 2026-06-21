package com.ridehailing.locationservice.util;

import lombok.NoArgsConstructor;

public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {}


    public static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }


    public static double bearing(double lat1, double lng1, double lat2, double lng2) {
        double dLng = Math.toRadians(lng2 - lng1);
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double y = Math.sin(dLng) * Math.cos(rLat2);
        double x = Math.cos(rLat1) * Math.sin(rLat2)
                - Math.sin(rLat1) * Math.cos(rLat2) * Math.cos(dLng);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }


    public static double speedKmh(double distanceKm, long elapsedSeconds) {
        if (elapsedSeconds <= 0) return 0.0;
        return (distanceKm / elapsedSeconds) * 3600.0;
    }


    public static void validate(double lat, double lng) {
        if (lat < -90 || lat > 90)
            throw new IllegalArgumentException("Latitude out of range: " + lat);
        if (lng < -180 || lng > 180)
            throw new IllegalArgumentException("Longitude out of range: " + lng);
    }
}
