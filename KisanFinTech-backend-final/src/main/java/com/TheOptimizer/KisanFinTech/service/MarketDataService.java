package com.TheOptimizer.KisanFinTech.service;

import com.TheOptimizer.KisanFinTech.dto.MarketRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MarketDataService {

    private final List<MarketRecord> records =
            new ArrayList<>();

    public MarketDataService() {

        loadMarketData();
    }

    private void loadMarketData() {

        try {

            ClassPathResource resource =
                    new ClassPathResource(
                            "data/crop_price_dataset.csv"
                    );

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            resource.getInputStream(),
                                            StandardCharsets.UTF_8
                                    )
                            )
            ) {

                String headerLine =
                        reader.readLine();

                if (headerLine == null) {
                    return;
                }

                String[] headers =
                        headerLine.split(",", -1);

                int monthIndex =
                        findColumn(
                                headers,
                                "month"
                        );

                int commodityIndex =
                        findColumn(
                                headers,
                                "commodity_name"
                        );

                int modalPriceIndex =
                        findColumn(
                                headers,
                                "avg_modal_price"
                        );

                int minPriceIndex =
                        findColumn(
                                headers,
                                "avg_min_price"
                        );

                int maxPriceIndex =
                        findColumn(
                                headers,
                                "avg_max_price"
                        );

                int changeIndex =
                        findColumn(
                                headers,
                                "change"
                        );

                int confidenceIndex =
                        findColumn(
                                headers,
                                "confidence"
                        );


                String line;

                while (
                        (line = reader.readLine())
                                != null
                ) {

                    if (line.isBlank()) {
                        continue;
                    }

                    String[] values =
                            line.split(",", -1);

                    int requiredIndex =
                            Math.max(
                                    commodityIndex,
                                    Math.max(
                                            modalPriceIndex,
                                            Math.max(
                                                    minPriceIndex,
                                                    maxPriceIndex
                                            )
                                    )
                            );

                    if (
                            values.length
                                    <= requiredIndex
                    ) {
                        continue;
                    }


                    try {

                        MarketRecord record =
                                new MarketRecord();

                        record.setMonth(
                                getValue(
                                        values,
                                        monthIndex
                                )
                        );

                        record.setCommodityName(
                                getValue(
                                        values,
                                        commodityIndex
                                )
                        );

                        record.setAvgModalPrice(
                                parseDouble(
                                        values,
                                        modalPriceIndex
                                )
                        );

                        record.setAvgMinPrice(
                                parseDouble(
                                        values,
                                        minPriceIndex
                                )
                        );

                        record.setAvgMaxPrice(
                                parseDouble(
                                        values,
                                        maxPriceIndex
                                )
                        );

                        record.setChange(
                                parseDouble(
                                        values,
                                        changeIndex
                                )
                        );

                        record.setConfidence(
                                parseDouble(
                                        values,
                                        confidenceIndex
                                )
                        );

                        records.add(record);

                    } catch (Exception ignored) {
                        // Ignore malformed row
                    }
                }
            }

            System.out.println(
                    "Loaded market records: "
                            + records.size()
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Could not load crop price dataset",
                    e
            );
        }
    }


    // =====================================================
    // GET ALL RECORDS FOR A CROP
    // =====================================================

    public List<MarketRecord> getRecordsForCrop(
            String crop
    ) {

        if (crop == null) {
            return List.of();
        }

        String normalized =
                normalize(crop);

        return records.stream()

                .filter(record ->
                        normalize(
                                record.getCommodityName()
                        ).equals(normalized)
                )

                .toList();
    }


    // =====================================================
    // AVERAGE MARKET PRICE
    // =====================================================

    public double getAveragePrice(
            String crop
    ) {

        List<MarketRecord> cropRecords =
                getRecordsForCrop(crop);

        return cropRecords.stream()

                .map(MarketRecord::getAvgModalPrice)

                .filter(value ->
                        value != null
                                && !Double.isNaN(value)
                )

                .mapToDouble(Double::doubleValue)

                .average()

                .orElse(0.0);
    }


    // =====================================================
    // MINIMUM MARKET PRICE
    // =====================================================

    public double getMinimumPrice(
            String crop
    ) {

        return getRecordsForCrop(crop)
                .stream()

                .map(MarketRecord::getAvgMinPrice)

                .filter(value ->
                        value != null
                                && !Double.isNaN(value)
                )

                .mapToDouble(Double::doubleValue)

                .min()

                .orElse(0.0);
    }


    // =====================================================
    // MAXIMUM MARKET PRICE
    // =====================================================

    public double getMaximumPrice(
            String crop
    ) {

        return getRecordsForCrop(crop)
                .stream()

                .map(MarketRecord::getAvgMaxPrice)

                .filter(value ->
                        value != null
                                && !Double.isNaN(value)
                )

                .mapToDouble(Double::doubleValue)

                .max()

                .orElse(0.0);
    }


    // =====================================================
    // AVERAGE CHANGE
    // =====================================================

    public double getAverageChange(
            String crop
    ) {

        return getRecordsForCrop(crop)
                .stream()

                .map(MarketRecord::getChange)

                .filter(value ->
                        value != null
                                && !Double.isNaN(value)
                )

                .mapToDouble(Double::doubleValue)

                .average()

                .orElse(0.0);
    }


    // =====================================================
    // AVERAGE CONFIDENCE
    // =====================================================

    public double getAverageConfidence(
            String crop
    ) {

        return getRecordsForCrop(crop)
                .stream()

                .map(MarketRecord::getConfidence)

                .filter(value ->
                        value != null
                                && !Double.isNaN(value)
                )

                .mapToDouble(Double::doubleValue)

                .average()

                .orElse(0.0);
    }


    // =====================================================
    // MONTHLY PRICES
    // =====================================================

    public List<MarketRecord> getMonthlyPrices(
            String crop
    ) {

        return getRecordsForCrop(crop);
    }


    // =====================================================
    // HELPERS
    // =====================================================

    private int findColumn(
            String[] headers,
            String target
    ) {

        for (int i = 0; i < headers.length; i++) {

            if (
                    normalize(headers[i])
                            .equals(
                                    normalize(target)
                            )
            ) {

                return i;
            }
        }

        return -1;
    }


    private String getValue(
            String[] values,
            int index
    ) {

        if (
                index < 0
                        || index >= values.length
        ) {

            return null;
        }

        return values[index]
                .trim();
    }


    private Double parseDouble(
            String[] values,
            int index
    ) {

        String value =
                getValue(
                        values,
                        index
                );

        if (
                value == null
                        || value.isBlank()
        ) {

            return null;
        }

        try {

            return Double.parseDouble(value);

        } catch (NumberFormatException e) {

            return null;
        }
    }


    private String normalize(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll(
                        "[^a-z0-9]",
                        ""
                );
    }
}