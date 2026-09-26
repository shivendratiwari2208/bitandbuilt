package com.TheOptimizer.KisanFinTech.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SoilClient {
    private final WebClient webClient;
    private final Map<String, Double> phosphorusBySoil = new HashMap<>();

    public SoilClient(WebClient webClient,
                      @Value("${soil.phosphorous.Black:}") String black,
                      @Value("${soil.phosphorous.Clayey:}") String clayey,
                      @Value("${soil.phosphorous.Loamy:}") String loamy,
                      @Value("${soil.phosphorous.Red:}") String red,
                      @Value("${soil.phosphorous.Sandy:}") String sandy) {
        this.webClient = webClient;
        putIfPresent("Black", black);
        putIfPresent("Clayey", clayey);
        putIfPresent("Loamy", loamy);
        putIfPresent("Red", red);
        putIfPresent("Sandy", sandy);
    }

    private void putIfPresent(String soil, String value) {
        if (value != null && !value.isBlank()) phosphorusBySoil.put(soil, Double.parseDouble(value));
    }

    public Map<String, Object> getSoilData(Double latitude, Double longitude) {
        Map<String, Object> response = webClient.get()
                .uri(uri -> uri.scheme("https")
                        .host("livingatlas.esri.in")
                        .path("/server1/rest/services/India/Agro_Ecological_Sub_Regions/MapServer/0/query")
                        .queryParam("geometry", longitude + "," + latitude)
                        .queryParam("geometryType", "esriGeometryPoint")
                        .queryParam("inSR", "4326")
                        .queryParam("spatialRel", "esriSpatialRelIntersects")
                        .queryParam("outFields", "soil_type,physio_reg,climate_")
                        .queryParam("returnGeometry", "false")
                        .queryParam("f", "json")
                        .build())
                .retrieve().bodyToMono(Map.class).block();

        Map<String, Object> attributes = firstAttributes(response);
        String raw = attributes == null || attributes.get("soil_type") == null ? null : attributes.get("soil_type").toString();
        String soilType = normalizeSoilType(raw);
        if (soilType == null) throw new IllegalArgumentException("Unsupported soil type returned for this location");

        Map<String, Object> result = new HashMap<>();
        Double phosphorous = phosphorusBySoil.get(soilType);
        if (phosphorous == null) throw new IllegalArgumentException("Phosphorous mapping is not configured for soil type: " + soilType);
        result.put("soilType", soilType);
        result.put("phosphorous", phosphorous);
        return result;
    }

    private String normalizeSoilType(String raw) {
        if (raw == null) return null;
        String soil = raw.toLowerCase();
        if (soil.contains("black")) return "Black";
        if (soil.contains("clay")) return "Clayey";
        if (soil.contains("loam")) return "Loamy";
        if (soil.contains("red")) return "Red";
        if (soil.contains("sand")) return "Sandy";
        return null;
    }

    private Map<String, Object> firstAttributes(Map<String, Object> data) {
        if (data == null || !(data.get("features") instanceof List<?> features) || features.isEmpty()) return null;
        Object first = features.get(0);
        if (!(first instanceof Map<?, ?> feature) || !(feature.get("attributes") instanceof Map<?, ?> attrs)) return null;
        Map<String, Object> result = new HashMap<>();
        attrs.forEach((k, v) -> result.put(String.valueOf(k), v));
        return result;
    }
}
