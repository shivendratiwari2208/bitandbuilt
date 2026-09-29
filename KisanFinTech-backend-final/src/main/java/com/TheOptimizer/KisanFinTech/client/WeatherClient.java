package com.TheOptimizer.KisanFinTech.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class WeatherClient {

    private final WebClient webClient;
    private final String weatherApiKey;

    public WeatherClient(
            WebClient webClient,
            @Value("${weather.api.key}") String weatherApiKey) {

        this.webClient = webClient;
        this.weatherApiKey = weatherApiKey;
    }

    public Map<String, Object> getWeather(
            Double latitude,
            Double longitude) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.weatherapi.com")
                        .path("/v1/forecast.json")

                        .queryParam("key", weatherApiKey)

                        .queryParam(
                                "q",
                                latitude + "," + longitude
                        )

                        .queryParam("days", 3)

                        .queryParam("alerts", "yes")

                        .queryParam("aqi", "no")

                        .build()
                )
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}