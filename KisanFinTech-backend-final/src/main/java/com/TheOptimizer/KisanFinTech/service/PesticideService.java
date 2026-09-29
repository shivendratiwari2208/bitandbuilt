package com.TheOptimizer.KisanFinTech.service;

import com.TheOptimizer.KisanFinTech.client.GeocodingClient;
import com.TheOptimizer.KisanFinTech.client.PesticideMlClient;
import com.TheOptimizer.KisanFinTech.client.SoilMoistureClient;
import com.TheOptimizer.KisanFinTech.client.WeatherClient;
import com.TheOptimizer.KisanFinTech.dto.LocationData;
import com.TheOptimizer.KisanFinTech.dto.PesticideRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PesticideService {

    private final GeocodingClient geocodingClient;
    private final WeatherClient weatherClient;
    private final SoilMoistureClient soilMoistureClient;
    private final PesticideMlClient pesticideMlClient;

    public PesticideService(
            GeocodingClient geocodingClient,
            WeatherClient weatherClient,
            SoilMoistureClient soilMoistureClient,
            PesticideMlClient pesticideMlClient
    ) {
        this.geocodingClient = geocodingClient;
        this.weatherClient = weatherClient;
        this.soilMoistureClient = soilMoistureClient;
        this.pesticideMlClient = pesticideMlClient;
    }

    public Map<String, Object> getRecommendation(
            PesticideRequest request
    ) {

        // =================================================
        // 1. GEOCODING
        // =================================================

        LocationData location =
                geocodingClient.getCoordinates(
                        request.getLocation(),
                        request.getPincode()
                );

        Double latitude =
                location.getLatitude();

        Double longitude =
                location.getLongitude();

        if (latitude == null || longitude == null) {
            throw new IllegalStateException(
                    "Unable to resolve coordinates for the given location and pincode."
            );
        }


        // =================================================
        // 2. WEATHER
        // =================================================

        Map<String, Object> weatherResponse =
                weatherClient.getWeather(
                        latitude,
                        longitude
                );

        Map<String, Object> current =
                extractMap(
                        weatherResponse,
                        "current"
                );


        Double temperature =
                numberValue(
                        current.get("temp_c")
                );

        Double humidity =
                numberValue(
                        current.get("humidity")
                );

        Double rainfall =
                numberValue(
                        current.get("precip_mm")
                );


        if (temperature == null) {
            throw new IllegalStateException(
                    "Temperature was not returned by weather service."
            );
        }

        if (humidity == null) {
            throw new IllegalStateException(
                    "Humidity was not returned by weather service."
            );
        }

        if (rainfall == null) {
            rainfall = 0.0;
        }


        // =================================================
        // 3. SOIL MOISTURE
        // =================================================

        Double soilMoisture =
                soilMoistureClient.getSoilMoisture(
                        latitude,
                        longitude
                );

        if (soilMoisture == null) {
            throw new IllegalStateException(
                    "Soil moisture was not returned."
            );
        }


        // Open-Meteo returns soil moisture as a fraction
        // such as:
        //
        // 0.364
        //
        // ML expects percentage:
        //
        // 36.4
        //

        Double soilMoisturePercent =
                soilMoisture <= 1.0
                        ? soilMoisture * 100.0
                        : soilMoisture;


        // =================================================
        // 4. CALL PESTICIDE ML
        // =================================================

        Map<String, Object> mlResult =
                pesticideMlClient.predict(
                        request.getCrop(),
                        temperature,
                        humidity,
                        rainfall,
                        soilMoisturePercent
                );


        if (mlResult == null) {
            throw new IllegalStateException(
                    "Pesticide ML service returned no response."
            );
        }


        // =================================================
        // 5. FINAL RESPONSE
        // =================================================

        Map<String, Object> result =
                new LinkedHashMap<>();


        result.put(
                "crop",
                request.getCrop()
        );

        result.put(
                "location",
                request.getLocation()
        );

        result.put(
                "pincode",
                request.getPincode()
        );

        result.put(
                "resolvedLocation",
                location.getDisplayName()
        );

        result.put(
                "coordinates",
                Map.of(
                        "latitude",
                        latitude,

                        "longitude",
                        longitude
                )
        );

        result.put(
                "environment",
                Map.of(
                        "temperature_C",
                        temperature,

                        "humidity_pct",
                        humidity,

                        "rainfall_mm",
                        rainfall,

                        "soil_moisture_pct",
                        soilMoisturePercent
                )
        );

        result.put(
                "pestRiskPct",
                mlResult.get(
                        "Pest_Risk_Pct"
                )
        );

        result.put(
                "riskLevel",
                mlResult.get(
                        "Risk_Level"
                )
        );

        result.put(
                "likelyPest",
                mlResult.get(
                        "Likely_Pest"
                )
        );

        result.put(
                "bestPesticide",
                mlResult.get(
                        "Best_Pesticide"
                )
        );

        return result;
    }


    // =====================================================
    // HELPERS
    // =====================================================

    private Map<String, Object> extractMap(
            Map<String, Object> source,
            String key
    ) {

        Object value =
                source.get(key);

        if (value instanceof Map<?, ?> map) {

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

        throw new IllegalStateException(
                "Weather response does not contain: "
                        + key
        );
    }


    private Double numberValue(
            Object value
    ) {

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        if (value == null) {
            return null;
        }

        try {

            return Double.parseDouble(
                    value.toString()
            );

        } catch (
                NumberFormatException e
        ) {

            return null;
        }
    }
}