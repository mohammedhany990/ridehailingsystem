package com.ridehailing.mapservice.client;


import com.ridehailing.mapservice.dto.OsrmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class OsrmClient {

    private final RestClient restClient;

    @Value("${osrm.base-url}")
    private String osrmBaseUrl;

    public OsrmResponse getRoute(
            double pickupLat,
            double pickupLng,
            double dropoffLat,
            double dropoffLng
    ) {

        String url =
                osrmBaseUrl +
                        "/route/v1/driving/"
                        + pickupLng + "," + pickupLat +
                        ";" +
                        dropoffLng + "," + dropoffLat +
                        "?overview=full&geometries=geojson";

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(OsrmResponse.class);
    }
}
