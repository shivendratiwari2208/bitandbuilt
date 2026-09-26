package com.TheOptimizer.KisanFinTech.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class WeatherClient {
    private final WebClient webClient;
    public WeatherClient(WebClient webClient) { this.webClient = webClient; }

    public Map<String, Object> getWeather(Double latitude, Double longitude) {
        return webClient.get()
                .uri(uri -> uri.scheme("https")
                        .host("api.open-meteo.com")
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "temperature_2m,relative_humidity_2m,precipitation,soil_moisture_0_to_1cm")
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
