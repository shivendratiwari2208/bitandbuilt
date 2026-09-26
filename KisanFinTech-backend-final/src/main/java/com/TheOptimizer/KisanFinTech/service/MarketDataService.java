package com.TheOptimizer.KisanFinTech.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketDataService {
    private static final List<String> MONTHS = List.of("January","February","March","April","May","June","July","August","September","October","November","December");
    private final Map<String, List<Row>> rowsByCrop = new HashMap<>();
    private final Map<String, Map<String, List<Row>>> rowsByCropMonth = new HashMap<>();

    @PostConstruct
    public void load() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("data/crop_price_dataset.csv").getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines().skip(1).forEach(line -> {
                String[] c = line.split(",", -1);
                if (c.length < 8) return;
                Row row = new Row(c[1].trim(), c[2].trim(), d(c[3]), d(c[4]), d(c[5]), d(c[6]), d(c[7]));
                rowsByCrop.computeIfAbsent(row.crop, k -> new ArrayList<>()).add(row);
                rowsByCropMonth.computeIfAbsent(row.crop, k -> new HashMap<>()).computeIfAbsent(row.month, k -> new ArrayList<>()).add(row);
            });
        } catch (Exception e) {
            throw new IllegalStateException("Could not load crop price dataset", e);
        }
    }

    public Map<String, Object> enrich(String crop, double modelProbability) {
        List<Row> rows = rowsByCrop.getOrDefault(crop, List.of());
        if (rows.isEmpty()) return Map.of("crop", crop, "confidence", round(modelProbability * 100));

        double expected = avg(rows, Row::modal);
        double min = avg(rows, Row::min);
        double max = avg(rows, Row::max);
        double avgChange = avg(rows, r -> Math.abs(r.change));
        double cv = coefficientOfVariation(rows.stream().map(r -> r.modal).collect(Collectors.toList()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("crop", crop);
        result.put("confidence", round(modelProbability * 100));
        result.put("expectedPrice", round(expected));
        result.put("minimumPrice", round(min));
        result.put("maximumPrice", round(max));
        result.put("averageChange", round(avgChange));
        result.put("volatility", round(cv));
        result.put("risk", risk(cv));
        return result;
    }

    public List<Map<String, Object>> monthlyPrices(List<String> crops) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String month : MONTHS) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("month", month.substring(0, 3));
            for (String crop : crops) {
                List<Row> rows = rowsByCropMonth.getOrDefault(crop, Map.of()).getOrDefault(month, List.of());
                if (!rows.isEmpty()) point.put(crop, round(avg(rows, Row::modal)));
            }
            result.add(point);
        }
        return result;
    }

    private static String risk(double cv) { if (cv < 20) return "Low"; if (cv < 40) return "Medium"; return "High"; }
    private static double d(String s) { return Double.parseDouble(s); }
    private static double avg(List<Row> rows, java.util.function.ToDoubleFunction<Row> f) { return rows.stream().mapToDouble(f).average().orElse(0); }
    private static double coefficientOfVariation(List<Double> values) { if (values.size() < 2) return 0; double mean=values.stream().mapToDouble(Double::doubleValue).average().orElse(0); if(mean==0)return 0; double variance=values.stream().mapToDouble(v->Math.pow(v-mean,2)).sum()/(values.size()-1); return Math.sqrt(variance)/mean*100; }
    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }
    private record Row(String month, String crop, double modal, double min, double max, double change, double confidence) {}
}
