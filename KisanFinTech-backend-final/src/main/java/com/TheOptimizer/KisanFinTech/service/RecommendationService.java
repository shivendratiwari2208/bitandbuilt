package com.TheOptimizer.KisanFinTech.service;

import com.TheOptimizer.KisanFinTech.client.GeocodingClient;
import com.TheOptimizer.KisanFinTech.client.MlClient;
import com.TheOptimizer.KisanFinTech.client.SoilClient;
import com.TheOptimizer.KisanFinTech.client.SoilMoistureClient;
import com.TheOptimizer.KisanFinTech.client.WeatherClient;
import com.TheOptimizer.KisanFinTech.dto.FeatureData;
import com.TheOptimizer.KisanFinTech.dto.LocationData;
import com.TheOptimizer.KisanFinTech.dto.RecommendationRequest;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private final GeocodingClient geocodingClient;
    private final WeatherClient weatherClient;
    private final SoilClient soilClient;
    private final SoilMoistureClient soilMoistureClient;
    private final MlClient mlClient;
    private final MarketService marketService;


    public RecommendationService(
            GeocodingClient geocodingClient,
            WeatherClient weatherClient,
            SoilClient soilClient,
            SoilMoistureClient soilMoistureClient,
            MlClient mlClient,
            MarketService marketService
    ) {

        this.geocodingClient =
                geocodingClient;

        this.weatherClient =
                weatherClient;

        this.soilClient =
                soilClient;

        this.soilMoistureClient =
                soilMoistureClient;

        this.mlClient =
                mlClient;

        this.marketService =
                marketService;
    }


    // =====================================================
    // MAIN RECOMMENDATION METHOD
    // =====================================================

    public Map<String, Object> getRecommendation(
            RecommendationRequest request
    ) {

        // -------------------------------------------------
        // 1. LOCATION
        // -------------------------------------------------

        LocationData location =
                geocodingClient.getCoordinates(
                        request.getLocation(),
                        request.getPincode()
                );


        // -------------------------------------------------
        // 2. WEATHER
        // -------------------------------------------------

        Map<String, Object> weather =
                weatherClient.getWeather(
                        location.getLatitude(),
                        location.getLongitude()
                );


        Map<String, Object> currentWeather =
                getMap(
                        weather,
                        "current"
                );


        double temperature =
                getDouble(
                        currentWeather,
                        "temp_c",
                        0.0
                );


        double humidity =
                getDouble(
                        currentWeather,
                        "humidity",
                        0.0
                );


        double rainfall =
                getDouble(
                        currentWeather,
                        "precip_mm",
                        0.0
                );


        // -------------------------------------------------
        // 3. SOIL
        // -------------------------------------------------

        Map<String, Object> soil =
                soilClient.getSoilData(
                        location.getLatitude(),
                        location.getLongitude()
                );


        String soilType =
                String.valueOf(
                        soil.getOrDefault(
                                "soilType",
                                "Black"
                        )
                );


        double phosphorous =
                getDouble(
                        soil,
                        "phosphorous",
                        10.0
                );


        // -------------------------------------------------
        // 4. SOIL MOISTURE
        // -------------------------------------------------

        double moisture;

        try {

            Double soilMoisture =
                    soilMoistureClient
                            .getSoilMoisture(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );

            moisture =
                    soilMoisture == null
                            ? 0.0
                            : soilMoisture;

        } catch (Exception e) {

            moisture = 0.0;
        }


        // -------------------------------------------------
        // 5. ML FEATURES
        // -------------------------------------------------

        FeatureData featureData =
                new FeatureData(
                        temperature,
                        humidity,
                        moisture,
                        soilType,
                        phosphorous
                );


        // -------------------------------------------------
        // 6. ML PREDICTION
        // -------------------------------------------------

        Map<String, Object> mlResult =
                mlClient.predict(
                        featureData
                );


        if (mlResult == null) {

            throw new IllegalStateException(
                    "ML service returned no response."
            );
        }


        // -------------------------------------------------
        // 7. CROP PREDICTIONS
        // -------------------------------------------------

        List<Map<String, Object>> predictions =
                extractPredictions(
                        mlResult
                );


        if (predictions.isEmpty()) {

            throw new IllegalStateException(
                    "ML service returned no crop predictions."
            );
        }


        // -------------------------------------------------
        // 8. TOP 3 CROPS
        // -------------------------------------------------

        List<Map<String, Object>> topCrops =
                predictions
                        .stream()
                        .sorted(
                                Comparator.comparingDouble(
                                        item ->
                                                -getDouble(
                                                        item,
                                                        "probability",
                                                        0.0
                                                )
                                )
                        )
                        .limit(3)
                        .toList();


        // -------------------------------------------------
        // 9. ENRICH TOP 3 WITH MARKET DATA
        // -------------------------------------------------

        List<Map<String, Object>> recommendations =
                new ArrayList<>();


        for (
                Map<String, Object> prediction
                : topCrops
        ) {

            String crop =
                    String.valueOf(
                            prediction.get(
                                    "crop"
                            )
                    );


            double probability =
                    getDouble(
                            prediction,
                            "probability",
                            0.0
                    );


            // ---------------------------------------------
            // MARKET DATA
            // ---------------------------------------------

            Map<String, Object> market =
                    marketService
                            .getMarketSummary(
                                    crop
                            );


            // ---------------------------------------------
            // PROFIT
            // ---------------------------------------------

            double area =
                    request.getArea();


            double estimatedProfit =
                    marketService
                            .calculateEstimatedProfit(
                                    crop,
                                    area
                            );


            // ---------------------------------------------
            // FINAL CROP OBJECT
            // ---------------------------------------------

            Map<String, Object> item =
                    new LinkedHashMap<>();


            item.put(
                    "crop",
                    crop
            );


            item.put(
                    "probability",
                    probability
            );


            item.put(
                    "probabilityPercent",
                    round(
                            probability * 100
                    )
            );


            item.put(
                    "confidence",
                    round(
                            probability * 100
                    )
            );


            item.put(
                    "market",
                    market
            );


            item.put(
                    "estimatedProfit",
                    round(
                            estimatedProfit
                    )
            );


            recommendations.add(
                    item
            );
        }


        // -------------------------------------------------
        // 10. SORT BY CONFIDENCE
        // -------------------------------------------------

        recommendations.sort(
                Comparator.comparingDouble(
                        item ->
                                -getDouble(
                                        item,
                                        "probability",
                                        0.0
                                )
                )
        );


        // =====================================================
        // MARKET TREND FOR GRAPH
        // =====================================================

        List<Map<String, Object>> marketTrend =
                new ArrayList<>();


        for (
                Map<String, Object> recommendation
                : recommendations
        ) {

            String crop =
                    String.valueOf(
                            recommendation.get(
                                    "crop"
                            )
                    );


            Object marketObject =
                    recommendation.get(
                            "market"
                    );


            if (
                    !(marketObject
                            instanceof Map<?, ?> marketMap)
            ) {

                continue;
            }


            Object monthlyObject =
                    marketMap.get(
                            "monthlyPrices"
                    );


            if (
                    !(monthlyObject
                            instanceof List<?> monthlyList)
            ) {

                continue;
            }


            for (
                    Object monthObject
                    : monthlyList
            ) {

                if (
                        !(monthObject
                                instanceof Map<?, ?> monthMap)
                ) {

                    continue;
                }


                Map<String, Object> trendItem =
                        new LinkedHashMap<>();


                trendItem.put(
                        "crop",
                        crop
                );


                trendItem.put(
                        "month",
                        monthMap.get(
                                "month"
                        )
                );


                trendItem.put(
                        "price",
                        monthMap.get(
                                "price"
                        )
                );


                marketTrend.add(
                        trendItem
                );
            }
        }


        // =====================================================
        // FERTILIZER
        // =====================================================

        Object recommendedFertilizer =
                mlResult.get(
                        "recommendedFertilizer"
                );


        Object fertilizerPredictions =
                mlResult.get(
                        "fertilizerPredictions"
                );


        // =====================================================
        // FINAL RESPONSE
        // =====================================================

        Map<String, Object> response =
                new LinkedHashMap<>();


        // -----------------------------------------------------
        // BASIC FARM DATA
        // -----------------------------------------------------

        response.put(
                "location",
                request.getLocation()
        );


        response.put(
                "pincode",
                request.getPincode()
        );


        response.put(
                "sowingDate",
                request.getSowingDate()
        );


        response.put(
                "area",
                request.getArea()
        );


        // -----------------------------------------------------
        // COORDINATES
        // -----------------------------------------------------

        Map<String, Object> coordinates =
                new LinkedHashMap<>();


        coordinates.put(
                "latitude",
                location.getLatitude()
        );


        coordinates.put(
                "longitude",
                location.getLongitude()
        );


        response.put(
                "coordinates",
                coordinates
        );


        // -----------------------------------------------------
        // SOIL
        // -----------------------------------------------------

        response.put(
                "soil",
                soil
        );


        // Frontend compatibility
        response.put(
                "soilType",
                soilType
        );


        response.put(
                "phosphorous",
                phosphorous
        );


        // -----------------------------------------------------
        // WEATHER
        // -----------------------------------------------------

        response.put(
                "weather",
                weather
        );


        // Frontend compatibility
        response.put(
                "temperature",
                temperature
        );


        response.put(
                "humidity",
                humidity
        );


        response.put(
                "rainfall",
                rainfall
        );


        response.put(
                "soilMoisture",
                moisture
        );


        // -----------------------------------------------------
        // CROP RECOMMENDATIONS
        // -----------------------------------------------------

        response.put(
                "recommendations",
                recommendations
        );


        // -----------------------------------------------------
        // TOP RECOMMENDED CROP
        // -----------------------------------------------------

        if (
                !recommendations.isEmpty()
        ) {

            response.put(
                    "recommendedCrop",
                    recommendations
                            .get(0)
                            .get("crop")
            );
        }


        // -----------------------------------------------------
        // MARKET TREND
        // -----------------------------------------------------

        response.put(
                "marketTrend",
                marketTrend
        );


        // -----------------------------------------------------
        // FERTILIZER
        // -----------------------------------------------------

        if (
                recommendedFertilizer != null
        ) {

            Map<String, Object> fertilizer =
                    new LinkedHashMap<>();


            fertilizer.put(
                    "recommended",
                    recommendedFertilizer
            );


            fertilizer.put(
                    "predictions",
                    fertilizerPredictions == null
                            ? List.of()
                            : fertilizerPredictions
            );


            response.put(
                    "fertilizer",
                    fertilizer
            );


            // Frontend compatibility
            response.put(
                    "recommendedFertilizer",
                    recommendedFertilizer
            );


            response.put(
                    "fertilizerPredictions",
                    fertilizerPredictions == null
                            ? List.of()
                            : fertilizerPredictions
            );
        }


        // -----------------------------------------------------
        // BACKWARD COMPATIBILITY
        // -----------------------------------------------------

        response.put(
                "cropPredictions",
                predictions
        );


        response.put(
                "predictions",
                predictions
        );


        return response;
    }


    // =====================================================
    // EXTRACT ML PREDICTIONS
    // =====================================================

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractPredictions(
            Map<String, Object> mlResult
    ) {

        Object value =
                mlResult.get(
                        "cropPredictions"
                );


        if (
                !(value instanceof List<?>)
        ) {

            value =
                    mlResult.get(
                            "predictions"
                    );
        }


        if (
                !(value instanceof List<?> list)
        ) {

            return List.of();
        }


        List<Map<String, Object>> result =
                new ArrayList<>();


        for (
                Object item : list
        ) {

            if (
                    item instanceof Map<?, ?> map
            ) {

                Map<String, Object> converted =
                        new LinkedHashMap<>();


                map.forEach(
                        (key, val) ->
                                converted.put(
                                        String.valueOf(key),
                                        val
                                )
                );


                result.add(
                        converted
                );
            }
        }


        return result;
    }


    // =====================================================
    // MAP HELPER
    // =====================================================

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(
            Map<String, Object> source,
            String key
    ) {

        if (source == null) {
            return Map.of();
        }


        Object value =
                source.get(key);


        if (
                value instanceof Map<?, ?> map
        ) {

            Map<String, Object> result =
                    new LinkedHashMap<>();


            map.forEach(
                    (k, v) ->
                            result.put(
                                    String.valueOf(k),
                                    v
                            )
            );


            return result;
        }


        return Map.of();
    }


    // =====================================================
    // DOUBLE HELPER
    // =====================================================

    private double getDouble(
            Map<String, Object> map,
            String key,
            double fallback
    ) {

        if (map == null) {
            return fallback;
        }


        Object value =
                map.get(key);


        if (
                value instanceof Number number
        ) {

            return number.doubleValue();
        }


        if (
                value instanceof String string
        ) {

            try {

                return Double.parseDouble(
                        string
                );

            } catch (
                    NumberFormatException ignored
            ) {
            }
        }


        return fallback;
    }


    // =====================================================
    // ROUND
    // =====================================================

    private double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}