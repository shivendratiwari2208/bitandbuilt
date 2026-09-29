package com.TheOptimizer.KisanFinTech.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MarketPriceService {

    private final List<MarketRecordInternal> records;

    public MarketPriceService() {
        this.records = loadRecords();
    }

    public PriceResult getPrice(
            String crop,
            String marketGroup,
            double fallbackPrice) {

        List<Double> prices =
                new ArrayList<>();

        String normalizedCrop =
                normalize(crop);

        String normalizedGroup =
                normalize(marketGroup);

        for (
                MarketRecordInternal record :
                records
        ) {

            String commodity =
                    normalize(
                            record.commodity()
                    );

            boolean cropMatch =
                    commodity.contains(
                            normalizedCrop
                    )
                            ||
                            normalizedCrop.contains(
                                    commodity
                            );

            boolean groupMatch =
                    !normalizedGroup.isBlank()
                            &&
                            (
                                    commodity.contains(
                                            normalizedGroup
                                    )
                                            ||
                                            normalizedGroup.contains(
                                                    commodity
                                            )
                            );

            if (cropMatch || groupMatch) {
                prices.add(record.price());
            }
        }

        if (!prices.isEmpty()) {

            double average =
                    prices.stream()
                            .mapToDouble(
                                    Double::doubleValue
                            )
                            .average()
                            .orElse(
                                    fallbackPrice
                            );

            return new PriceResult(
                    round(average),
                    "Historical crop-price dataset",
                    prices.size()
            );
        }

        return new PriceResult(
                round(fallbackPrice),
                "Crop catalog benchmark",
                0
        );
    }

    private List<MarketRecordInternal> loadRecords() {

        List<MarketRecordInternal> result =
                new ArrayList<>();

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

                String header =
                        reader.readLine();

                if (header == null) {
                    return result;
                }

                String[] columns =
                        header.split(
                                ",",
                                -1
                        );

                int commodityIndex =
                        findColumn(
                                columns,
                                "commodity_name"
                        );

                int priceIndex =
                        findColumn(
                                columns,
                                "avg_modal_price"
                        );

                if (
                        commodityIndex == -1
                                ||
                                priceIndex == -1
                ) {

                    throw new IllegalStateException(
                            "Required columns not found in crop_price_dataset.csv"
                    );
                }

                String line;

                while (
                        (line =
                                reader.readLine())
                                != null
                ) {

                    if (line.isBlank()) {
                        continue;
                    }

                    String[] parts =
                            line.split(
                                    ",",
                                    -1
                            );

                    if (
                            parts.length <=
                                    Math.max(
                                            commodityIndex,
                                            priceIndex
                                    )
                    ) {
                        continue;
                    }

                    String commodity =
                            clean(
                                    parts[
                                            commodityIndex
                                            ]
                            );

                    String priceText =
                            clean(
                                    parts[
                                            priceIndex
                                            ]
                            );

                    try {

                        double price =
                                Double.parseDouble(
                                        priceText
                                );

                        if (
                                !commodity.isBlank()
                                        &&
                                        price > 0
                        ) {

                            result.add(
                                    new MarketRecordInternal(
                                            commodity,
                                            price
                                    )
                            );
                        }

                    } catch (
                            NumberFormatException ignored) {
                    }
                }
            }

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to load crop price dataset.",
                    e
            );
        }

        return result;
    }

    private int findColumn(
            String[] columns,
            String expected) {

        for (
                int i = 0;
                i < columns.length;
                i++
        ) {

            String column =
                    clean(columns[i])
                            .toLowerCase(
                                    Locale.ROOT
                            );

            if (column.equals(expected)) {
                return i;
            }
        }

        return -1;
    }

    private String clean(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replace("\"", "");
    }

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replace("&", "and")
                .replaceAll(
                        "[^a-z0-9]+",
                        ""
                );
    }

    private double round(
            double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    private record MarketRecordInternal(
            String commodity,
            double price
    ) {
    }

    public record PriceResult(
            double averagePrice,
            String source,
            int observations
    ) {
    }
}