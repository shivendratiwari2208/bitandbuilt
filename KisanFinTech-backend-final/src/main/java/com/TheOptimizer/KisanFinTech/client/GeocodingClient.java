package com.TheOptimizer.KisanFinTech.client;

import com.TheOptimizer.KisanFinTech.dto.LocationData;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GeocodingClient {
    private final WebClient webClient;
    public GeocodingClient(WebClient webClient) { this.webClient = webClient; }

    public LocationData getCoordinates(String location, String pincode) {
        return webClient.get()
                .uri(uri -> uri.scheme("https")
                        .host("nominatim.openstreetmap.org")
                        .path("/search")
                        .queryParam("q", location + ", " + pincode)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .header("User-Agent", "KisanFinTech-Hackathon/1.0")
                .retrieve()
                .bodyToMono(LocationData[].class)
                .map(result -> {
                    if (result == null || result.length == 0) throw new IllegalArgumentException("Location not found");
                    return result[0];
                }).block();
    }
}
