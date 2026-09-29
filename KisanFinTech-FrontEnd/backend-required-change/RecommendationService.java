package com.TheOptimizer.KisanFinTech.service;

import com.TheOptimizer.KisanFinTech.client.GeocodingClient;
import com.TheOptimizer.KisanFinTech.client.MlClient;
import com.TheOptimizer.KisanFinTech.client.SoilClient;
import com.TheOptimizer.KisanFinTech.client.WeatherClient;
import com.TheOptimizer.KisanFinTech.dto.FeatureData;
import com.TheOptimizer.KisanFinTech.dto.LocationData;
import com.TheOptimizer.KisanFinTech.dto.RecommendationRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {

    private final GeocodingClient geocodingClient;
    private final WeatherClient weatherClient;
    private final SoilClient soilClient;
    private final MlClient mlClient;
    private final MarketDataService marketDataService;

    public RecommendationService(
            GeocodingClient geocodingClient,
            WeatherClient weatherClient,
            SoilClient soilClient,
            MlClient mlClient,
            MarketDataService marketDataService) {
        this.geocodingClient = geocodingClient;
        this.weatherClient = weatherClient;
        this.soilClient = soilClient;
        this.mlClient = mlClient;
        this.marketDataService = marketDataService;
    }

    public Map<String, Object> getRecommendation(RecommendationRequest request) {

        LocationData location = geocodingClient.getCoordinates(
                request.getLocation(),
                request.getPincode()
        );

        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();

        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException(
                    "Coordinates were not returned for the location"
            );
        }

        Map<String, Object> weatherRaw = weatherClient.getWeather(
                latitude,
                longitude
        );

        Map<String, Object> current = asMap(
                weatherRaw.get("current")
        );

        Map<String, Object> soil = soilClient.getSoilData(
                latitude,
                longitude
        );

        Double temperature = number(current.get("temperature_2m"));
        Double humidity = number(current.get("relative_humidity_2m"));
        Double moisture = number(current.get("soil_moisture_0_to_1cm"));
        Double phosphorous = number(soil.get("phosphorous"));

        FeatureData features = new FeatureData();
        features.setTemperature(temperature);
        features.setHumidity(humidity);
        features.setMoisture(moisture);
        features.setSoilType(String.valueOf(soil.get("soilType")));
        features.setPhosphorous(phosphorous);

        Map<String, Object> ml = mlClient.predict(features);

        List<Map<String, Object>> predictions = predictionList(
                ml.get("predictions")
        );

        if (predictions.isEmpty()) {
            predictions = predictionList(ml.get("cropPredictions"));
        }

        if (predictions.isEmpty()) {
            throw new IllegalStateException(
                    "ML API returned no crop predictions"
            );
        }

        predictions.sort(
                (a, b) -> Double.compare(
                        number(b.get("probability")),
                        number(a.get("probability"))
                )
        );

        List<Map<String, Object>> top3 = new ArrayList<>();

        for (int i = 0; i < Math.min(3, predictions.size()); i++) {
            Map<String, Object> prediction = predictions.get(i);

            Map<String, Object> market = marketDataService.enrich(
                    String.valueOf(prediction.get("crop")),
                    number(prediction.get("probability"))
            );

            // Preserve useful ML probability fields.
            if (prediction.containsKey("probabilityPercent")) {
                market.put(
                        "probabilityPercent",
                        prediction.get("probabilityPercent")
                );
            }

            top3.add(market);
        }

        List<String> crops = top3.stream()
                .map(item -> String.valueOf(item.get("crop")))
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("location", request.getLocation());
        response.put("pincode", request.getPincode());
        response.put("sowingDate", request.getSowingDate());
        response.put("displayName", location.getDisplayName());
        response.put("latitude", latitude);
        response.put("longitude", longitude);

        response.put("weather", Map.of(
                "temperature", round(temperature),
                "humidity", round(humidity),
                "rainfall", round(number(current.get("precipitation"))),
                "soilMoisture", round(moisture * 100.0),
                "soilType", soil.get("soilType")
        ));

        response.put(
                "recommendedCrop",
                ml.get("recommendedCrop")
        );

        response.put("recommendations", top3);

        response.put(
                "marketPrices",
                marketDataService.monthlyPrices(crops)
        );

        // -------------------------------------------------
        // Fertilizer data returned by Flask ML service
        // -------------------------------------------------
        if (ml.containsKey("recommendedFertilizer")) {
            response.put(
                    "recommendedFertilizer",
                    ml.get("recommendedFertilizer")
            );
        }

        if (ml.containsKey("fertilizerPredictions")) {
            response.put(
                    "fertilizerPredictions",
                    ml.get("fertilizerPredictions")
            );
        }

        if (ml.containsKey("recommendedCropMarket")) {
            response.put(
                    "recommendedCropMarket",
                    ml.get("recommendedCropMarket")
            );
        }

        return response;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();
    }

    private static List<Map<String, Object>> predictionList(Object value) {
        if (!(value instanceof List<?> list)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> converted = new HashMap<>();
                map.forEach(
                        (key, val) -> converted.put(
                                String.valueOf(key),
                                val
                        )
                );
                result.add(converted);
            }
        }

        return result;
    }

    private static double number(Object value) {
        return value instanceof Number number
                ? number.doubleValue()
                : 0.0;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
