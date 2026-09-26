package com.TheOptimizer.KisanFinTech.service;

import com.TheOptimizer.KisanFinTech.dto.MarketRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class MarketService {

    private final List<MarketRecord> records = new ArrayList<>();

    public MarketService() {
        loadCsv();
    }

    private void loadCsv() {

        try {
            ClassPathResource resource =
                    new ClassPathResource("data/crop_price_dataset.csv");

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         resource.getInputStream(),
                                         StandardCharsets.UTF_8))) {

                String line;

                // Skip header
                reader.readLine();

                while ((line = reader.readLine()) != null) {

                    String[] values = line.split(",", -1);

                    if (values.length < 7) {
                        continue;
                    }

                    MarketRecord record = new MarketRecord();

                    record.setMonth(values[0].trim());
                    record.setCommodityName(values[1].trim());

                    record.setAvgModalPrice(
                            parseDouble(values[2])
                    );

                    record.setAvgMinPrice(
                            parseDouble(values[3])
                    );

                    record.setAvgMaxPrice(
                            parseDouble(values[4])
                    );

                    record.setChange(
                            parseDouble(values[5])
                    );

                    record.setConfidence(
                            parseDouble(values[6])
                    );

                    records.add(record);
                }
            }

            System.out.println(
                    "Loaded market records: " + records.size()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load market CSV", e
            );
        }
    }

    private Double parseDouble(String value) {

        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }

            return Double.parseDouble(value.trim());

        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<MarketRecord> getRecords() {
        return records;
    }
    public List<MarketRecord> findByCrop(String crop) {

        return records.stream()
                .filter(record ->
                        record.getCommodityName()
                                .equalsIgnoreCase(crop))
                .toList();
    }
}