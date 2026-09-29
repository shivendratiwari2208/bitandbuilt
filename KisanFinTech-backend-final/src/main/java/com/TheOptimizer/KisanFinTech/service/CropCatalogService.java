package com.TheOptimizer.KisanFinTech.service;

import com.TheOptimizer.KisanFinTech.dto.CropCalendarEntry;
import com.TheOptimizer.KisanFinTech.dto.SowingWindow;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class CropCatalogService {

    private final List<CropCalendarEntry> crops;

    public CropCatalogService() {
        this.crops = loadCatalog();
    }

    public List<CropCalendarEntry> getAllCrops() {
        return Collections.unmodifiableList(crops);
    }

    private List<CropCalendarEntry> loadCatalog() {

        List<CropCalendarEntry> result =
                new ArrayList<>();

        try {

            ClassPathResource resource =
                    new ClassPathResource(
                            "data/mp_crop_catalog.csv"
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

                String line;
                boolean firstLine = true;

                while ((line = reader.readLine()) != null) {

                    line = line.trim();

                    if (line.isBlank()) {
                        continue;
                    }

                    if (line.startsWith("#")) {
                        continue;
                    }

                    if (firstLine) {

                        firstLine = false;

                        if (line
                                .toLowerCase()
                                .startsWith("crop|")) {

                            continue;
                        }
                    }

                    String[] parts =
                            line.split(
                                    "\\|",
                                    -1
                            );

                    if (parts.length < 16) {
                        continue;
                    }

                    result.add(
                            parseCrop(parts)
                    );
                }
            }

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to load MP crop catalog.",
                    e
            );
        }

        if (result.isEmpty()) {

            throw new IllegalStateException(
                    "MP crop catalog is empty."
            );
        }

        return result;
    }

    private CropCalendarEntry parseCrop(
            String[] p) {

        CropCalendarEntry crop =
                new CropCalendarEntry();

        crop.setCrop(p[0].trim());
        crop.setCategory(p[1].trim());
        crop.setMarketGroup(p[2].trim());

        crop.setStatewideMajor(
                Boolean.parseBoolean(
                        p[3].trim()
                )
        );

        crop.setHarvestDaysMin(
                Integer.parseInt(
                        p[4].trim()
                )
        );

        crop.setHarvestDaysMax(
                Integer.parseInt(
                        p[5].trim()
                )
        );

        crop.setAvgInvestmentPerHectare(
                Double.parseDouble(
                        p[6].trim()
                )
        );

        crop.setAvgYieldQuintalPerHectare(
                Double.parseDouble(
                        p[7].trim()
                )
        );

        crop.setBenchmarkPricePerQuintal(
                Double.parseDouble(
                        p[8].trim()
                )
        );

        crop.setSoilTypes(
                splitList(p[9])
        );

        crop.setMinTemperatureC(
                Double.parseDouble(
                        p[10].trim()
                )
        );

        crop.setMaxTemperatureC(
                Double.parseDouble(
                        p[11].trim()
                )
        );

        crop.setSource(p[12].trim());
        crop.setNote(p[13].trim());

        crop.setSeasons(
                splitList(p[14])
        );

        crop.setSowingWindows(
                parseWindows(p[15])
        );

        return crop;
    }

    private List<String> splitList(
            String value) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return new ArrayList<>();
        }

        return Arrays.stream(
                        value.split(";")
                )
                .map(String::trim)
                .filter(
                        s -> !s.isBlank()
                )
                .toList();
    }

    private List<SowingWindow> parseWindows(
            String value) {

        List<SowingWindow> windows =
                new ArrayList<>();

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return windows;
        }

        for (
                String entry :
                value.split(";")
        ) {

            String[] parts =
                    entry.trim()
                            .split(":");

            if (parts.length != 2) {
                continue;
            }

            String season =
                    parts[0].trim();

            String[] range =
                    parts[1]
                            .trim()
                            .split("-");

            if (range.length != 4) {
                continue;
            }

            try {

                int startMonth =
                        Integer.parseInt(
                                range[0]
                        );

                int startDay =
                        Integer.parseInt(
                                range[1]
                        );

                int endMonth =
                        Integer.parseInt(
                                range[2]
                        );

                int endDay =
                        Integer.parseInt(
                                range[3]
                        );

                String label =
                        season
                                + " ("
                                + String.format(
                                "%02d-%02d",
                                startMonth,
                                startDay
                        )
                                + " to "
                                + String.format(
                                "%02d-%02d",
                                endMonth,
                                endDay
                        )
                                + ")";

                windows.add(
                        new SowingWindow(
                                season,
                                startMonth,
                                startDay,
                                endMonth,
                                endDay,
                                label
                        )
                );

            } catch (
                    NumberFormatException ignored) {
            }
        }

        return windows;
    }
}