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

    public RecommendationService(GeocodingClient geocodingClient, WeatherClient weatherClient, SoilClient soilClient, MlClient mlClient, MarketDataService marketDataService) {
        this.geocodingClient=geocodingClient; this.weatherClient=weatherClient; this.soilClient=soilClient; this.mlClient=mlClient; this.marketDataService=marketDataService;
    }

    public Map<String,Object> getRecommendation(RecommendationRequest request) {
        LocationData location = geocodingClient.getCoordinates(request.getLocation(), request.getPincode());
        Double lat=location.getLatitude(), lon=location.getLongitude();
        if(lat==null || lon==null) throw new IllegalArgumentException("Coordinates were not returned for the location");

        Map<String,Object> weatherRaw=weatherClient.getWeather(lat,lon);
        Map<String,Object> current=asMap(weatherRaw.get("current"));
        Map<String,Object> soil=soilClient.getSoilData(lat,lon);

        FeatureData features=new FeatureData();
        features.setTemperature(number(current.get("temperature_2m")));
        features.setHumidity(number(current.get("relative_humidity_2m")));
        features.setMoisture(number(current.get("soil_moisture_0_to_1cm")));
        features.setSoilType((String)soil.get("soilType"));
        features.setPhosphorous(number(soil.get("phosphorous")));

        Map<String,Object> ml=mlClient.predict(features);
        List<Map<String,Object>> predictions=predictionList(ml.get("predictions"));
        if(predictions.isEmpty()) throw new IllegalStateException("ML API returned no crop predictions");
        predictions.sort((a,b)->Double.compare(number(b.get("probability")),number(a.get("probability"))));

        List<Map<String,Object>> top3=new ArrayList<>();
        for(int i=0;i<Math.min(3,predictions.size());i++) {
            Map<String,Object> p=predictions.get(i);
            top3.add(marketDataService.enrich(String.valueOf(p.get("crop")),number(p.get("probability"))));
        }
        List<String> crops=top3.stream().map(x->String.valueOf(x.get("crop"))).toList();

        Map<String,Object> response=new LinkedHashMap<>();
        response.put("location",request.getLocation());
        response.put("pincode",request.getPincode());
        response.put("sowingDate",request.getSowingDate());
        response.put("displayName",location.getDisplayName());
        response.put("latitude",lat);
        response.put("longitude",lon);
        response.put("weather",Map.of(
                "temperature", round(number(current.get("temperature_2m"))),
                "humidity", round(number(current.get("relative_humidity_2m"))),
                "rainfall", round(number(current.get("precipitation"))),
                "soilMoisture", round(number(current.get("soil_moisture_0_to_1cm"))*100),
                "soilType", soil.get("soilType")
        ));
        response.put("recommendedCrop",ml.get("recommendedCrop"));
        response.put("recommendations",top3);
        response.put("marketPrices",marketDataService.monthlyPrices(crops));
        return response;
    }

    @SuppressWarnings("unchecked") private static Map<String,Object> asMap(Object value){ return value instanceof Map<?,?> m ? (Map<String,Object>)m : Map.of(); }
    private static List<Map<String,Object>> predictionList(Object value){
        if(!(value instanceof List<?> list)) return new ArrayList<>();
        List<Map<String,Object>> out=new ArrayList<>();
        for(Object item:list) if(item instanceof Map<?,?> m){ Map<String,Object> x=new HashMap<>(); m.forEach((k,v)->x.put(String.valueOf(k),v)); out.add(x); }
        return out;
    }
    private static double number(Object v){ return v instanceof Number n ? n.doubleValue() : 0; }
    private static double round(double v){ return Math.round(v*100.0)/100.0; }
}
