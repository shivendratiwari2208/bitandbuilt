package com.TheOptimizer.KisanFinTech.client;

import com.TheOptimizer.KisanFinTech.dto.FeatureData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class MlClient {
    private final WebClient webClient;
    private final String mlApiUrl;

    public MlClient(WebClient webClient, @Value("${ml.api.url}") String mlApiUrl) {
        this.webClient = webClient;
        this.mlApiUrl = mlApiUrl;
    }

    public Map<String, Object> predict(FeatureData featureData) {
        return webClient.post()
                .uri(mlApiUrl + "/predict")
                .bodyValue(featureData)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
