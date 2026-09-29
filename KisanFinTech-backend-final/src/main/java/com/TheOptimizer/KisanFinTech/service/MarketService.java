package com.TheOptimizer.KisanFinTech.service;

import com.TheOptimizer.KisanFinTech.dto.MarketRecord;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarketService {

    private final MarketDataService marketDataService;

    public MarketService(
            MarketDataService marketDataService
    ) {

        this.marketDataService =
                marketDataService;
    }


    // =====================================================
    // MARKET SUMMARY FOR ONE CROP
    // =====================================================

    public Map<String, Object> getMarketSummary(
            String crop
    ) {

        List<MarketRecord> records =
                marketDataService
                        .getRecordsForCrop(crop);


        double averagePrice =
                marketDataService
                        .getAveragePrice(crop);

        double minimumPrice =
                marketDataService
                        .getMinimumPrice(crop);

        double maximumPrice =
                marketDataService
                        .getMaximumPrice(crop);

        double averageChange =
                marketDataService
                        .getAverageChange(crop);

        double confidence =
                marketDataService
                        .getAverageConfidence(crop);


        String risk =
                calculateRisk(
                        averageChange
                );


        Map<String, Object> result =
                new LinkedHashMap<>();


        result.put(
                "crop",
                crop
        );

        result.put(
                "expectedPrice",
                round(averagePrice)
        );

        result.put(
                "minimumPrice",
                round(minimumPrice)
        );

        result.put(
                "maximumPrice",
                round(maximumPrice)
        );

        result.put(
                "averageChange",
                round(averageChange)
        );

        result.put(
                "confidence",
                round(confidence)
        );

        result.put(
                "risk",
                risk
        );

        result.put(
                "observations",
                records.size()
        );


        result.put(
                "monthlyPrices",
                records
                        .stream()
                        .map(record -> {

                            Map<String, Object> item =
                                    new LinkedHashMap<>();

                            item.put(
                                    "month",
                                    record.getMonth()
                            );

                            item.put(
                                    "price",
                                    round(
                                            record.getAvgModalPrice()
                                    )
                            );

                            return item;

                        })
                        .toList()
        );


        return result;
    }


    // =====================================================
    // ESTIMATED PROFIT
    // =====================================================

    public double calculateEstimatedProfit(
            String crop,
            double areaAcres
    ) {

        double averagePrice =
                marketDataService
                        .getAveragePrice(crop);


        /*
         * Existing recommendation UI uses
         * area-based profit estimation.
         *
         * This is a transparent planning estimate.
         *
         * If you have a trained yield/cost model,
         * it can replace this calculation later.
         */

        double estimatedYieldPerAcre =
                getEstimatedYieldPerAcre(crop);


        double revenue =
                averagePrice
                        * estimatedYieldPerAcre
                        * areaAcres;


        double estimatedCost =
                getEstimatedCostPerAcre(crop)
                        * areaAcres;


        return revenue - estimatedCost;
    }


    // =====================================================
    // YIELD BENCHMARK
    // =====================================================

    private double getEstimatedYieldPerAcre(
            String crop
    ) {

        return switch (
                normalize(crop)
                ) {

            case "soybean" ->
                    8.0;

            case "maize" ->
                    20.0;

            case "wheat" ->
                    18.0;

            case "paddy" ->
                    20.0;

            case "barley" ->
                    16.0;

            case "millets" ->
                    12.0;

            case "oilseeds" ->
                    8.0;

            case "groundnuts" ->
                    10.0;

            case "pulses" ->
                    7.0;

            case "sugarcane" ->
                    300.0;

            case "tobacco" ->
                    8.0;

            default ->
                    10.0;
        };
    }


    // =====================================================
    // COST BENCHMARK
    // =====================================================

    private double getEstimatedCostPerAcre(
            String crop
    ) {

        return switch (
                normalize(crop)
                ) {

            case "soybean" ->
                    24000;

            case "maize" ->
                    20000;

            case "wheat" ->
                    18000;

            case "paddy" ->
                    23000;

            case "barley" ->
                    17000;

            case "millets" ->
                    15000;

            case "oilseeds" ->
                    18000;

            case "groundnuts" ->
                    22000;

            case "pulses" ->
                    16000;

            case "sugarcane" ->
                    50000;

            case "tobacco" ->
                    40000;

            default ->
                    18000;
        };
    }


    // =====================================================
    // RISK
    // =====================================================

    private String calculateRisk(
            double change
    ) {

        double absoluteChange =
                Math.abs(change);


        if (absoluteChange >= 20) {

            return "High";

        } else if (absoluteChange >= 10) {

            return "Medium";

        } else {

            return "Low";
        }
    }


    // =====================================================
    // NORMALIZE
    // =====================================================

    private String normalize(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9]",
                        ""
                );
    }


    private double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }


    private double round(
            Double value
    ) {

        if (value == null) {
            return 0.0;
        }

        return round(
                value.doubleValue()
        );
    }
}