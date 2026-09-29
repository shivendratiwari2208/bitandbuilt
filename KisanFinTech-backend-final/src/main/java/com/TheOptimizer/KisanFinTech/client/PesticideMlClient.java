package com.TheOptimizer.KisanFinTech.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class PesticideMlClient {

    private final WebClient webClient;
    private final String pesticideMlApiUrl;

    public PesticideMlClient(
            WebClient webClient,
            @Value("${pesticide.ml.api.url}") String pesticideMlApiUrl
    ) {
        this.webClient = webClient;
        this.pesticideMlApiUrl = pesticideMlApiUrl;
    }

    public Map<String, Object> predict(
            String crop,
            Double temperature,
            Double humidity,
            Double rainfall,
            Double soilMoisture
    ) {

        Map<String, Object> request =
                Map.of(
                        "crop", crop,
                        "temperature", temperature,
                        "humidity", humidity,
                        "rainfall", rainfall,
                        "soilMoisture", soilMoisture
                );

        return webClient
                .post()
                .uri(
                        pesticideMlApiUrl
                                + "/predict"
                )
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}