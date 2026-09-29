package com.TheOptimizer.KisanFinTech.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class SoilMoistureClient {

    private final WebClient webClient;

    public SoilMoistureClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Double getSoilMoisture(
            Double latitude,
            Double longitude) {

        Map<String, Object> response =
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .scheme("https")
                                .host("api.open-meteo.com")
                                .path("/v1/forecast")

                                .queryParam(
                                        "latitude",
                                        latitude
                                )

                                .queryParam(
                                        "longitude",
                                        longitude
                                )

                                .queryParam(
                                        "current",
                                        "soil_moisture_0_to_1cm"
                                )

                                .queryParam(
                                        "timezone",
                                        "auto"
                                )

                                .build()
                        )
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

        if (response == null) {
            return null;
        }

        Object currentObject =
                response.get("current");

        if (!(currentObject instanceof Map<?, ?> current)) {
            return null;
        }

        Object moisture =
                current.get(
                        "soil_moisture_0_to_1cm"
                );

        if (moisture instanceof Number number) {
            return number.doubleValue();
        }

        return null;
    }
}