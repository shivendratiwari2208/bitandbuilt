package com.TheOptimizer.KisanFinTech.service;

import com.TheOptimizer.KisanFinTech.client.GeocodingClient;
import com.TheOptimizer.KisanFinTech.client.SoilClient;
import com.TheOptimizer.KisanFinTech.client.SoilMoistureClient;
import com.TheOptimizer.KisanFinTech.client.WeatherClient;
import com.TheOptimizer.KisanFinTech.dto.CropCalendarEntry;
import com.TheOptimizer.KisanFinTech.dto.LocationData;
import com.TheOptimizer.KisanFinTech.dto.SowingPlannerRequest;
import com.TheOptimizer.KisanFinTech.dto.SowingWindow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SowingPlannerService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE;

    /*
     * ---------------------------------------------------------
     * MONTH-BASED PLANNING
     * ---------------------------------------------------------
     *
     * 0 = current month
     * 1 = next month
     * 2 = month after next
     *
     * Crops whose sowing window is within these months can be
     * considered for recommendation.
     *
     * Exact day is NOT a hard rejection anymore.
     */
    private static final int MONTH_LOOKAHEAD = 2;


    private final GeocodingClient geocodingClient;
    private final WeatherClient weatherClient;
    private final SoilClient soilClient;
    private final SoilMoistureClient soilMoistureClient;
    private final CropCatalogService cropCatalogService;
    private final MarketPriceService marketPriceService;
    private final String plannerRegion;


    public SowingPlannerService(
            GeocodingClient geocodingClient,
            WeatherClient weatherClient,
            SoilClient soilClient,
            SoilMoistureClient soilMoistureClient,
            CropCatalogService cropCatalogService,
            MarketPriceService marketPriceService,
            @Value("${planner.region:Madhya Pradesh}")
            String plannerRegion) {

        this.geocodingClient = geocodingClient;
        this.weatherClient = weatherClient;
        this.soilClient = soilClient;
        this.soilMoistureClient = soilMoistureClient;
        this.cropCatalogService = cropCatalogService;
        this.marketPriceService = marketPriceService;
        this.plannerRegion = plannerRegion;
    }


    // =========================================================
    // MAIN PLANNER
    // =========================================================

    public Map<String, Object> createPlan(
            SowingPlannerRequest request) {

        LocalDate sowingDate =
                request.getSowingDate();

        LocalDate harvestDate =
                request.getHarvestDate();


        // -----------------------------------------------------
        // DATE VALIDATION
        // -----------------------------------------------------

        if (sowingDate == null) {
            throw new IllegalArgumentException(
                    "Sowing date is required."
            );
        }

        if (harvestDate == null) {
            throw new IllegalArgumentException(
                    "Harvest date is required."
            );
        }

        if (!harvestDate.isAfter(sowingDate)) {
            throw new IllegalArgumentException(
                    "Harvest date must be after sowing date."
            );
        }


        // -----------------------------------------------------
        // LOCATION
        // -----------------------------------------------------

        if (request.getLocation() == null
                || request.getLocation().isBlank()) {

            throw new IllegalArgumentException(
                    "Location is required."
            );
        }


        LocationData location =
                geocodingClient.getCoordinates(
                        request.getLocation().trim(),
                        request.getPincode().trim()
                );

        validateMadhyaPradesh(location);


        // -----------------------------------------------------
        // WEATHER
        // -----------------------------------------------------

        Map<String, Object> weather =
                weatherClient.getWeather(
                        location.getLatitude(),
                        location.getLongitude()
                );


        // -----------------------------------------------------
        // SOIL
        // -----------------------------------------------------

        Map<String, Object> soil =
                soilClient.getSoilData(
                        location.getLatitude(),
                        location.getLongitude()
                );


        // -----------------------------------------------------
        // SOIL MOISTURE
        // -----------------------------------------------------

        Double soilMoistureRaw = null;

        try {

            soilMoistureRaw =
                    soilMoistureClient.getSoilMoisture(
                            location.getLatitude(),
                            location.getLongitude()
                    );

        } catch (Exception ignored) {

            /*
             * Soil moisture is enrichment data.
             * Planner continues even if Open-Meteo fails.
             */
        }


        if (soilMoistureRaw != null) {

            soil.put(
                    "soilMoistureRaw",
                    soilMoistureRaw
            );

            soil.put(
                    "soilMoisturePercent",
                    round(soilMoistureRaw * 100.0)
            );

            soil.put(
                    "soilMoistureSource",
                    "Open-Meteo"
            );

        } else {

            soil.put(
                    "soilMoistureRaw",
                    null
            );

            soil.put(
                    "soilMoisturePercent",
                    null
            );

            soil.put(
                    "soilMoistureSource",
                    "Open-Meteo (unavailable)"
            );
        }


        // -----------------------------------------------------
        // CURRENT ENVIRONMENT
        // -----------------------------------------------------

        String detectedSoil =
                String.valueOf(
                        soil.getOrDefault(
                                "soilType",
                                "Unknown"
                        )
                );


        Double currentTemperature =
                extractCurrentTemperature(
                        weather
                );


        // -----------------------------------------------------
        // EVALUATE EVERY CROP
        // -----------------------------------------------------

        List<Map<String, Object>> crops =
                new ArrayList<>();


        for (
                CropCalendarEntry crop
                : cropCatalogService.getAllCrops()
        ) {

            buildCropPlan(
                    crop,
                    sowingDate,
                    harvestDate,
                    detectedSoil,
                    currentTemperature,
                    crops
            );
        }


        // -----------------------------------------------------
        // SORT
        // -----------------------------------------------------

        crops.sort(
                Comparator
                        .comparingInt(
                                this::recommendationOrder
                        )
                        .thenComparingInt(
                                this::soilOrder
                        )
                        .thenComparingInt(
                                this::weatherOrder
                        )
                        .thenComparing(
                                (Map<String, Object> item) ->
                                        -profitValue(item)
                        )
                        .thenComparing(
                                item ->
                                        String.valueOf(
                                                item.get("crop")
                                        )
                        )
        );


        // -----------------------------------------------------
        // RECOMMENDED CROPS
        // -----------------------------------------------------

        List<Map<String, Object>> recommended =
                crops.stream()
                        .filter(
                                item ->
                                        "RECOMMENDED"
                                                .equals(
                                                        item.get(
                                                                "status"
                                                        )
                                                )
                        )
                        .toList();


        // -----------------------------------------------------
        // SOIL COMPATIBLE / UNKNOWN
        // -----------------------------------------------------

        List<Map<String, Object>> soilCompatible =
                crops.stream()
                        .filter(
                                item ->
                                        "COMPATIBLE".equals(
                                                item.get(
                                                        "soilCompatibility"
                                                )
                                        )
                                                ||
                                                "UNKNOWN".equals(
                                                        item.get(
                                                                "soilCompatibility"
                                                        )
                                                )
                        )
                        .toList();


        // -----------------------------------------------------
        // SUMMARY
        // -----------------------------------------------------

        Map<String, Object> summary =
                new LinkedHashMap<>();


        summary.put(
                "totalCropsConsidered",
                crops.size()
        );


        summary.put(
                "recommendedCount",
                recommended.size()
        );


        summary.put(
                "soilCompatibleOrUnknownCount",
                soilCompatible.size()
        );


        summary.put(
                "plannedDurationDays",
                ChronoUnit.DAYS.between(
                        sowingDate,
                        harvestDate
                )
        );


        summary.put(
                "detectedSoilType",
                "Unknown".equalsIgnoreCase(
                        detectedSoil
                )
                        ? null
                        : detectedSoil
        );


        summary.put(
                "currentTemperatureC",
                currentTemperature
        );


        summary.put(
                "planningMonth",
                sowingDate.getMonth()
                        .getDisplayName(
                                java.time.format.TextStyle.FULL,
                                Locale.ENGLISH
                        )
        );


        summary.put(
                "monthBasedPlanning",
                true
        );


        summary.put(
                "monthLookahead",
                MONTH_LOOKAHEAD
        );


        // -----------------------------------------------------
        // RESPONSE
        // -----------------------------------------------------

        Map<String, Object> response =
                new LinkedHashMap<>();


        response.put(
                "feature",
                "Seasonal Crop Planner"
        );


        response.put(
                "region",
                plannerRegion
        );


        response.put(
                "location",
                request.getLocation().trim()
        );


        response.put(
                "resolvedLocation",
                location.getDisplayName()
        );


        response.put(
                "coordinates",
                mapOf(
                        "latitude",
                        location.getLatitude(),

                        "longitude",
                        location.getLongitude()



                )
        );


        response.put(
                "planningDates",
                mapOf(
                        "sowingDate",
                        sowingDate.format(
                                DATE_FORMAT
                        ),

                        "harvestDate",
                        harvestDate.format(
                                DATE_FORMAT
                        ),

                        "plannedDurationDays",
                        ChronoUnit.DAYS.between(
                                sowingDate,
                                harvestDate
                        )
                )
        );


        response.put(
                "planningPeriod",
                detectPlanningPeriod(
                        sowingDate
                )
        );


        response.put(
                "summary",
                summary
        );


        response.put(
                "weather",
                buildWeatherResponse(
                        weather,
                        currentTemperature,
                        soilMoistureRaw
                )
        );


        response.put(
                "soil",
                soil
        );


        response.put(
                "recommendedCrops",
                recommended
        );


        response.put(
                "allCrops",
                crops
        );


        response.put(
                "catalogCoverage",
                mapOf(
                        "state",
                        plannerRegion,

                        "catalogType",
                        "State-level major crop planning catalog",

                        "cropCount",
                        crops.size(),

                        "note",
                        "District-level sowing windows and economics can vary; replace catalog benchmark values with district/source data when available."
                )
        );


        response.put(
                "economicsMethod",
                mapOf(
                        "investment",
                        "Configured planning benchmark per hectare",

                        "revenue",
                        "Expected yield per hectare × average/benchmark price per quintal",

                        "profit",
                        "Revenue − investment",

                        "priceSource",
                        "Supplied historical crop-price dataset when crop exists; otherwise catalog benchmark",

                        "warning",
                        "These are planning estimates, not guaranteed farmer returns."
                )
        );


        response.put(
                "planningMethod",
                mapOf(

                        "primaryFactor",
                        "Sowing month and seasonal window",

                        "secondaryFactors",
                        List.of(
                                "Soil compatibility",
                                "Current temperature compatibility",
                                "Statewide crop importance",
                                "Planning economics"
                        ),

                        "harvestDateUsage",
                        "Warning and planning guidance, not a hard crop rejection",

                        "exactSowingDateUsage",
                        "Used to identify whether the crop window is current or upcoming; exact day mismatch does not automatically reject the crop."
                )
        );


        response.put(
                "dataSources",
                List.of(
                        "Nominatim / OpenStreetMap for geocoding",
                        "WeatherAPI.com for live weather and short forecast",
                        "Matribhoomi SoilFertility map service for soil attributes when available",
                        "Open-Meteo for current shallow soil moisture",
                        "Madhya Pradesh crop planning catalog",
                        "Supplied crop-price dataset"
                )
        );


        return response;
    }


    // =========================================================
    // CROP EVALUATION
    // =========================================================

    private void buildCropPlan(
            CropCalendarEntry crop,
            LocalDate sowingDate,
            LocalDate harvestDate,
            String detectedSoil,
            Double currentTemperature,
            List<Map<String, Object>> output) {


        // -----------------------------------------------------
        // FIND MONTH-BASED MATCH
        // -----------------------------------------------------

        int monthDistance =
                monthDistanceToCropWindow(
                        crop.getSowingWindows(),
                        sowingDate
                );


        SowingWindow exactWindow =
                findContainingWindow(
                        crop.getSowingWindows(),
                        sowingDate
                );


        SowingWindow monthWindow =
                findBestMonthWindow(
                        crop.getSowingWindows(),
                        sowingDate
                );


        SowingWindow nextWindow =
                findNextWindow(
                        crop.getSowingWindows(),
                        sowingDate
                );


        long plannedDuration =
                ChronoUnit.DAYS.between(
                        sowingDate,
                        harvestDate
                );


        // -----------------------------------------------------
        // HARVEST DATE
        // -----------------------------------------------------

        boolean harvestTooEarly =
                plannedDuration
                        < crop.getHarvestDaysMin();


        boolean harvestTooLate =
                plannedDuration
                        > crop.getHarvestDaysMax();


        String harvestFit;


        if (harvestTooEarly) {

            harvestFit =
                    "TOO_EARLY";

        } else if (harvestTooLate) {

            harvestFit =
                    "TOO_LATE";

        } else {

            harvestFit =
                    "ALIGNED";
        }


        LocalDate expectedHarvestFrom =
                sowingDate.plusDays(
                        crop.getHarvestDaysMin()
                );


        LocalDate expectedHarvestTo =
                sowingDate.plusDays(
                        crop.getHarvestDaysMax()
                );


        // -----------------------------------------------------
        // SOIL
        // -----------------------------------------------------

        String soilCompatibility =
                soilCompatibility(
                        crop.getSoilTypes(),
                        detectedSoil
                );


        // -----------------------------------------------------
        // WEATHER
        // -----------------------------------------------------

        String weatherCompatibility =
                weatherCompatibility(
                        crop,
                        currentTemperature
                );


        // -----------------------------------------------------
        // MONTH MATCH
        // -----------------------------------------------------

        String monthMatch;


        if (monthDistance == 0) {

            monthMatch =
                    "CURRENT_MONTH";

        } else if (monthDistance == 1) {

            monthMatch =
                    "NEXT_MONTH";

        } else if (monthDistance == 2) {

            monthMatch =
                    "UPCOMING";

        } else {

            monthMatch =
                    "OUTSIDE_PLANNING_HORIZON";
        }


        // -----------------------------------------------------
        // RECOMMENDATION
        // -----------------------------------------------------

        boolean monthRelevant =
                monthDistance >= 0
                        &&
                        monthDistance <= MONTH_LOOKAHEAD;


        boolean soilAcceptable =
                "COMPATIBLE".equals(
                        soilCompatibility
                )
                        ||
                        "UNKNOWN".equals(
                                soilCompatibility
                        );


        boolean weatherAcceptable =
                "COMPATIBLE_CURRENT_CONDITION".equals(
                        weatherCompatibility
                )
                        ||
                        "UNKNOWN".equals(
                                weatherCompatibility
                        );


        /*
         * A crop is recommended when:
         *
         * 1. Its sowing month is current/upcoming.
         * 2. Soil is compatible or soil data is unavailable.
         * 3. Current temperature is compatible or unavailable.
         *
         * Exact sowing day and harvest date are NOT hard filters.
         */

        boolean recommended =
                monthRelevant
                        &&
                        soilAcceptable
                        &&
                        weatherAcceptable;


        String status =
                recommended
                        ? "RECOMMENDED"
                        : "NOT_RECOMMENDED";


        String recommendationType;


        if (monthDistance == 0) {

            recommendationType =
                    exactWindow != null
                            ? "CURRENT_SOWING_WINDOW"
                            : "CURRENT_MONTH_WINDOW";

        } else if (monthDistance == 1) {

            recommendationType =
                    "NEXT_MONTH";

        } else if (monthDistance == 2) {

            recommendationType =
                    "UPCOMING";

        } else {

            recommendationType =
                    "OUTSIDE_PLANNING_HORIZON";
        }


        // -----------------------------------------------------
        // REASON
        // -----------------------------------------------------

        String reason;


        if (recommended) {

            StringBuilder message =
                    new StringBuilder();


            if (exactWindow != null) {

                message.append(
                        "The selected sowing date is inside the configured "
                                + crop.getCrop()
                                + " sowing window."
                );

            } else if (monthWindow != null) {

                message.append(
                        "The selected sowing month aligns with the "
                                + crop.getCrop()
                                + " seasonal window."
                );

            } else {

                message.append(
                        "The crop is within the upcoming seasonal planning horizon."
                );
            }


            if (monthDistance == 1) {

                message.append(
                        " Its next sowing period begins next month."
                );

            } else if (monthDistance == 2) {

                message.append(
                        " Its sowing period is approaching within the next two months."
                );
            }


            if (harvestTooEarly) {

                message.append(
                        " Warning: the selected harvest date is earlier "
                                + "than the crop's normal minimum duration."
                );

            } else if (harvestTooLate) {

                message.append(
                        " Warning: the selected harvest date is later "
                                + "than the crop's normal maximum duration."
                );
            }


            if ("NOT_IDEAL".equals(
                    soilCompatibility
            )) {

                message.append(
                        " Soil compatibility is weaker for the detected soil."
                );
            }


            if ("OUTSIDE_CURRENT_TEMPERATURE_RANGE".equals(
                    weatherCompatibility
            )) {

                message.append(
                        " Current temperature is outside the catalog's preferred range."
                );
            }


            reason =
                    message.toString();

        } else {

            if (!monthRelevant) {

                reason =
                        "The crop's sowing season is outside the current "
                                + "month-based planning horizon.";

            } else if (!soilAcceptable) {

                reason =
                        "The crop is seasonally relevant, but the detected "
                                + "soil type is not in its preferred soil list.";

            } else if (!weatherAcceptable) {

                reason =
                        "The crop is seasonally relevant, but the current "
                                + "temperature is outside its configured range.";

            } else {

                reason =
                        "The crop does not currently meet the planner's "
                                + "recommendation conditions.";
            }
        }


        // -----------------------------------------------------
        // MARKET PRICE
        // -----------------------------------------------------

        MarketPriceService.PriceResult price =
                marketPriceService.getPrice(
                        crop.getCrop(),
                        crop.getMarketGroup(),
                        crop.getBenchmarkPricePerQuintal()
                );


        // -----------------------------------------------------
        // ECONOMICS
        // -----------------------------------------------------

        double investment =
                crop.getAvgInvestmentPerHectare();


        double averageYield =
                crop.getAvgYieldQuintalPerHectare();


        double averagePrice =
                price.averagePrice();


        double revenue =
                averageYield
                        * averagePrice;


        double profit =
                revenue
                        - investment;


        double margin =
                revenue == 0
                        ? 0
                        : (
                        profit
                                /
                                revenue
                )
                        * 100.0;


        // -----------------------------------------------------
        // RESULT OBJECT
        // -----------------------------------------------------

        Map<String, Object> item =
                new LinkedHashMap<>();


        item.put(
                "crop",
                crop.getCrop()
        );


        item.put(
                "category",
                crop.getCategory()
        );


        item.put(
                "marketGroup",
                crop.getMarketGroup()
        );


        item.put(
                "season",
                monthWindow != null
                        ? monthWindow.getSeason()
                        : primarySeason(crop)
        );


        item.put(
                "status",
                status
        );


        item.put(
                "sowingStatus",
                status
        );


        item.put(
                "recommendationType",
                recommendationType
        );


        item.put(
                "monthMatch",
                monthMatch
        );


        item.put(
                "monthDistance",
                monthDistance
        );


        item.put(
                "recommendationBasis",
                List.of(
                        "Sowing month/season",
                        "Soil compatibility",
                        "Current temperature compatibility",
                        "Planning economics"
                )
        );


        item.put(
                "reason",
                reason
        );


        // -----------------------------------------------------
        // SOWING WINDOW
        // -----------------------------------------------------

        item.put(
                "sowingWindow",
                monthWindow != null
                        ? monthWindow.getLabel()
                        : formatAllWindows(
                        crop.getSowingWindows()
                )
        );


        item.put(
                "currentExactWindow",
                exactWindow == null
                        ? null
                        : exactWindow.getLabel()
        );


        item.put(
                "nextSowingWindow",
                nextWindow == null
                        ? null
                        : nextWindow.getLabel()
        );


        item.put(
                "daysUntilNextWindow",
                nextWindow == null
                        ? null
                        : daysUntilNextWindow(
                        nextWindow,
                        sowingDate
                )
        );


        // -----------------------------------------------------
        // HARVEST
        // -----------------------------------------------------

        item.put(
                "plannedHarvestDate",
                harvestDate.format(
                        DATE_FORMAT
                )
        );


        item.put(
                "expectedHarvestFrom",
                expectedHarvestFrom.format(
                        DATE_FORMAT
                )
        );


        item.put(
                "expectedHarvestTo",
                expectedHarvestTo.format(
                        DATE_FORMAT
                )
        );


        item.put(
                "harvestDateFit",
                harvestFit
        );


        item.put(
                "harvestWarning",
                harvestTooEarly
                        ? "Selected harvest date is earlier than the normal crop duration."
                        : harvestTooLate
                        ? "Selected harvest date is later than the normal crop duration."
                        : null
        );


        item.put(
                "cropDurationDays",
                mapOf(
                        "minimum",
                        crop.getHarvestDaysMin(),

                        "maximum",
                        crop.getHarvestDaysMax(),

                        "planned",
                        plannedDuration
                )
        );


        // -----------------------------------------------------
        // SOIL
        // -----------------------------------------------------

        item.put(
                "soilCompatibility",
                soilCompatibility
        );


        item.put(
                "preferredSoils",
                crop.getSoilTypes()
        );


        // -----------------------------------------------------
        // WEATHER
        // -----------------------------------------------------

        item.put(
                "currentWeatherCompatibility",
                weatherCompatibility
        );


        item.put(
                "temperatureRange",
                mapOf(
                        "minimumC",
                        crop.getMinTemperatureC(),

                        "maximumC",
                        crop.getMaxTemperatureC(),

                        "currentC",
                        currentTemperature
                )
        );


        // -----------------------------------------------------
        // ECONOMICS
        // -----------------------------------------------------

        item.put(
                "economics",
                mapOf(

                        "averageInvestmentPerHectare",
                        round(investment),

                        "averageYieldQuintalPerHectare",
                        round(averageYield),

                        "averageMarketPricePerQuintal",
                        round(averagePrice),

                        "estimatedRevenueAveragePerHectare",
                        round(revenue),

                        "estimatedProfitAveragePerHectare",
                        round(profit),

                        "estimatedProfitMarginPercent",
                        round(margin),

                        "priceObservations",
                        price.observations(),

                        "priceSource",
                        price.source()
                )
        );


        item.put(
                "statewideMajor",
                crop.isStatewideMajor()
        );


        item.put(
                "source",
                crop.getSource()
        );


        item.put(
                "note",
                crop.getNote()
        );


        output.add(item);
    }


    // =========================================================
    // MONTH MATCHING
    // =========================================================

    private int monthDistanceToCropWindow(
            List<SowingWindow> windows,
            LocalDate date) {

        if (
                windows == null
                        ||
                        windows.isEmpty()
        ) {

            return 99;
        }


        int currentMonth =
                date.getMonthValue();


        int best =
                99;


        for (
                SowingWindow window
                : windows
        ) {

            int distance =
                    monthDistance(
                            currentMonth,
                            window
                    );


            best =
                    Math.min(
                            best,
                            distance
                    );
        }


        return best;
    }


    private int monthDistance(
            int currentMonth,
            SowingWindow window) {

        List<Integer> months =
                monthsCoveredByWindow(
                        window
                );


        int best =
                99;


        for (
                Integer month
                : months
        ) {

            int forward =
                    (
                            month
                                    -
                                    currentMonth
                                    +
                                    12
                    )
                            %
                            12;


            best =
                    Math.min(
                            best,
                            forward
                    );
        }


        return best;
    }


    private List<Integer> monthsCoveredByWindow(
            SowingWindow window) {

        List<Integer> months =
                new ArrayList<>();


        int start =
                window.getStartMonth();


        int end =
                window.getEndMonth();


        int month =
                start;


        while (true) {

            months.add(
                    month
            );


            if (month == end) {
                break;
            }


            month++;


            if (month > 12) {
                month = 1;
            }


            /*
             * Safety against malformed catalog data.
             */
            if (months.size() > 12) {
                break;
            }
        }


        return months;
    }


    private SowingWindow findBestMonthWindow(
            List<SowingWindow> windows,
            LocalDate date) {

        if (
                windows == null
                        ||
                        windows.isEmpty()
        ) {

            return null;
        }


        SowingWindow best =
                null;


        int bestDistance =
                Integer.MAX_VALUE;


        for (
                SowingWindow window
                : windows
        ) {

            int distance =
                    monthDistance(
                            date.getMonthValue(),
                            window
                    );


            if (
                    distance
                            <
                            bestDistance
            ) {

                bestDistance =
                        distance;

                best =
                        window;
            }
        }


        return best;
    }


    // =========================================================
    // EXACT WINDOW
    // =========================================================

    private SowingWindow findContainingWindow(
            List<SowingWindow> windows,
            LocalDate date) {

        MonthDay md =
                MonthDay.from(
                        date
                );


        for (
                SowingWindow window
                : windows
        ) {

            MonthDay start =
                    MonthDay.of(
                            window.getStartMonth(),
                            window.getStartDay()
                    );


            MonthDay end =
                    MonthDay.of(
                            window.getEndMonth(),
                            window.getEndDay()
                    );


            if (
                    isMonthDayWithin(
                            md,
                            start,
                            end
                    )
            ) {

                return window;
            }
        }


        return null;
    }


    // =========================================================
    // NEXT WINDOW
    // =========================================================

    private SowingWindow findNextWindow(
            List<SowingWindow> windows,
            LocalDate date) {

        if (
                windows == null
                        ||
                        windows.isEmpty()
        ) {

            return null;
        }


        SowingWindow best =
                null;


        long bestDays =
                Long.MAX_VALUE;


        for (
                SowingWindow window
                : windows
        ) {

            MonthDay start =
                    MonthDay.of(
                            window.getStartMonth(),
                            window.getStartDay()
                    );


            long days =
                    daysUntilMonthDay(
                            date,
                            start
                    );


            if (
                    days > 0
                            &&
                            days < bestDays
            ) {

                bestDays =
                        days;

                best =
                        window;
            }
        }


        return best;
    }


    private long daysUntilNextWindow(
            SowingWindow window,
            LocalDate date) {

        return daysUntilMonthDay(
                date,
                MonthDay.of(
                        window.getStartMonth(),
                        window.getStartDay()
                )
        );
    }


    private long daysUntilMonthDay(
            LocalDate date,
            MonthDay target) {

        LocalDate candidate =
                target.atYear(
                        date.getYear()
                );


        if (
                !candidate.isAfter(
                        date
                )
        ) {

            candidate =
                    candidate.plusYears(
                            1
                    );
        }


        return ChronoUnit.DAYS.between(
                date,
                candidate
        );
    }


    private boolean isMonthDayWithin(
            MonthDay value,
            MonthDay start,
            MonthDay end) {

        if (
                !end.isBefore(
                        start
                )
        ) {

            return !value.isBefore(
                    start
            )
                    &&
                    !value.isAfter(
                            end
                    );
        }


        /*
         * Window crosses New Year.
         * Example: Nov → Jan.
         */

        return !value.isBefore(
                start
        )
                ||
                !value.isAfter(
                        end
                );
    }


    // =========================================================
    // SOIL
    // =========================================================

    private String soilCompatibility(
            List<String> preferredSoils,
            String detectedSoil) {

        if (
                detectedSoil == null
                        ||
                        detectedSoil.isBlank()
                        ||
                        "unknown".equalsIgnoreCase(
                                detectedSoil
                        )
        ) {

            return "UNKNOWN";
        }


        for (
                String preferred
                : preferredSoils
        ) {

            if (
                    normaliseSoil(
                            preferred
                    )
                            .equals(
                                    normaliseSoil(
                                            detectedSoil
                                    )
                            )
            ) {

                return "COMPATIBLE";
            }
        }


        return "NOT_IDEAL";
    }


    private String normaliseSoil(
            String value) {

        String v =
                value.toLowerCase(
                        Locale.ROOT
                );


        if (
                v.contains("black")
                        ||
                        v.contains("cotton")
        ) {

            return "black";
        }


        if (
                v.contains("clay")
        ) {

            return "clayey";
        }


        if (
                v.contains("loam")
                        ||
                        v.contains("domat")
                        ||
                        v.contains("alluvial")
        ) {

            return "loamy";
        }


        if (
                v.contains("sand")
        ) {

            return "sandy";
        }


        if (
                v.contains("red")
                        ||
                        v.contains("later")
        ) {

            return "red";
        }


        return v;
    }


    // =========================================================
    // WEATHER
    // =========================================================

    private String weatherCompatibility(
            CropCalendarEntry crop,
            Double temperature) {

        if (temperature == null) {
            return "UNKNOWN";
        }


        if (
                temperature
                        >= crop.getMinTemperatureC()
                        &&
                        temperature
                                <= crop.getMaxTemperatureC()
        ) {

            return "COMPATIBLE_CURRENT_CONDITION";
        }


        return "OUTSIDE_CURRENT_TEMPERATURE_RANGE";
    }


    // =========================================================
    // SORTING
    // =========================================================

    private int recommendationOrder(
            Map<String, Object> item) {

        String monthMatch =
                String.valueOf(
                        item.get(
                                "monthMatch"
                        )
                );


        return switch (
                monthMatch
                ) {

            case "CURRENT_MONTH" ->
                    0;

            case "NEXT_MONTH" ->
                    1;

            case "UPCOMING" ->
                    2;

            default ->
                    3;
        };
    }


    private int soilOrder(
            Map<String, Object> item) {

        return switch (
                String.valueOf(
                        item.get(
                                "soilCompatibility"
                        )
                )
                ) {

            case "COMPATIBLE" ->
                    0;

            case "UNKNOWN" ->
                    1;

            default ->
                    2;
        };
    }


    private int weatherOrder(
            Map<String, Object> item) {

        return switch (
                String.valueOf(
                        item.get(
                                "currentWeatherCompatibility"
                        )
                )
                ) {

            case "COMPATIBLE_CURRENT_CONDITION" ->
                    0;

            case "UNKNOWN" ->
                    1;

            default ->
                    2;
        };
    }


    private double profitValue(
            Map<String, Object> item) {

        Object economics =
                item.get(
                        "economics"
                );


        if (
                economics
                        instanceof Map<?, ?> map
        ) {

            Object value =
                    map.get(
                            "estimatedProfitAveragePerHectare"
                    );


            return toDouble(
                    value,
                    0.0
            );
        }


        return 0.0;
    }


    // =========================================================
    // WEATHER RESPONSE
    // =========================================================

    private Map<String, Object> buildWeatherResponse(
            Map<String, Object> weather,
            Double currentTemperature,
            Double soilMoistureRaw) {

        Map<String, Object> result =
                new LinkedHashMap<>();


        result.put(
                "source",
                "WeatherAPI.com"
        );


        result.put(
                "current",
                weather.get(
                        "current"
                )
        );


        result.put(
                "location",
                weather.get(
                        "location"
                )
        );


        result.put(
                "forecast",
                weather.get(
                        "forecast"
                )
        );


        result.put(
                "alerts",
                weather.get(
                        "alerts"
                )
        );


        result.put(
                "currentTemperatureC",
                currentTemperature
        );


        result.put(
                "currentHumidityPercent",
                extractCurrentHumidity(
                        weather
                )
        );


        result.put(
                "currentRainfallMm",
                extractCurrentNumber(
                        weather,
                        "precip_mm"
                )
        );


        result.put(
                "rainProbabilityPercent",
                extractTodayRainChance(
                        weather
                )
        );


        result.put(
                "windSpeedKph",
                extractCurrentNumber(
                        weather,
                        "wind_kph"
                )
        );


        result.put(
                "windGustKph",
                extractCurrentNumber(
                        weather,
                        "gust_kph"
                )
        );


        result.put(
                "windDirection",
                extractCurrentString(
                        weather,
                        "wind_dir"
                )
        );


        result.put(
                "windDegree",
                extractCurrentNumber(
                        weather,
                        "wind_degree"
                )
        );


        result.put(
                "uvIndex",
                extractCurrentNumber(
                        weather,
                        "uv"
                )
        );


        result.put(
                "cloudCoverPercent",
                extractCurrentNumber(
                        weather,
                        "cloud"
                )
        );


        result.put(
                "pressureMb",
                extractCurrentNumber(
                        weather,
                        "pressure_mb"
                )
        );


        result.put(
                "visibilityKm",
                extractCurrentNumber(
                        weather,
                        "vis_km"
                )
        );


        result.put(
                "condition",
                extractCurrentCondition(
                        weather
                )
        );


        result.put(
                "soilMoisturePercent",
                soilMoistureRaw == null
                        ? null
                        : round(
                        soilMoistureRaw
                                * 100.0
                )
        );


        result.put(
                "soilMoistureSource",
                "Open-Meteo"
        );


        return result;
    }


    // =========================================================
    // WEATHER HELPERS
    // =========================================================

    @SuppressWarnings("unchecked")
    private Map<String, Object> current(
            Map<String, Object> weather) {

        Object value =
                weather.get(
                        "current"
                );


        if (
                value
                        instanceof Map<?, ?> map
        ) {

            return (Map<String, Object>) map;
        }


        return Map.of();
    }


    private Double extractCurrentTemperature(
            Map<String, Object> weather) {

        return toDouble(
                current(
                        weather
                ).get(
                        "temp_c"
                ),
                null
        );
    }


    private Double extractCurrentHumidity(
            Map<String, Object> weather) {

        return toDouble(
                current(
                        weather
                ).get(
                        "humidity"
                ),
                null
        );
    }


    private Double extractCurrentNumber(
            Map<String, Object> weather,
            String key) {

        return toDouble(
                current(
                        weather
                ).get(
                        key
                ),
                null
        );
    }


    private String extractCurrentString(
            Map<String, Object> weather,
            String key) {

        Object value =
                current(
                        weather
                ).get(
                        key
                );


        return value == null
                ? null
                : String.valueOf(
                value
        );
    }


    @SuppressWarnings("unchecked")
    private String extractCurrentCondition(
            Map<String, Object> weather) {

        Object value =
                current(
                        weather
                ).get(
                        "condition"
                );


        if (
                value
                        instanceof Map<?, ?> map
        ) {

            Object text =
                    map.get(
                            "text"
                    );


            return text == null
                    ? null
                    : String.valueOf(
                    text
            );
        }


        return value == null
                ? null
                : String.valueOf(
                value
        );
    }


    @SuppressWarnings("unchecked")
    private Integer extractTodayRainChance(
            Map<String, Object> weather) {

        Object forecastObject =
                weather.get(
                        "forecast"
                );


        if (
                !(forecastObject
                        instanceof Map<?, ?> forecast)
        ) {

            return null;
        }


        Object daysObject =
                forecast.get(
                        "forecastday"
                );


        if (
                !(daysObject
                        instanceof List<?> days)
                        ||
                        days.isEmpty()
        ) {

            return null;
        }


        Object first =
                days.get(
                        0
                );


        if (
                !(first
                        instanceof Map<?, ?> firstDay)
        ) {

            return null;
        }


        Object dayObject =
                firstDay.get(
                        "day"
                );


        if (
                !(dayObject
                        instanceof Map<?, ?> day)
        ) {

            return null;
        }


        return toInteger(
                day.get(
                        "daily_chance_of_rain"
                )
        );
    }


    // =========================================================
    // PLANNING PERIOD
    // =========================================================

    private Map<String, Object> detectPlanningPeriod(
            LocalDate date) {

        int month =
                date.getMonthValue();


        String season;
        String label;


        if (
                month >= 6
                        &&
                        month <= 9
        ) {

            season =
                    "KHARIF";

            label =
                    "Kharif / Rabi transition planning window";

        } else if (
                month >= 10
                        ||
                        month <= 3
        ) {

            season =
                    "RABI";

            label =
                    "Rabi planning window";

        } else {

            season =
                    "ZAID_SUMMER";

            label =
                    "Zaid / summer planning window";
        }


        return mapOf(

                "season",
                season,

                "label",
                label,

                "referenceDate",
                date.format(
                        DATE_FORMAT
                ),

                "month",
                date.getMonthValue(),

                "monthName",
                date.getMonth()
                        .getDisplayName(
                                java.time.format.TextStyle.FULL,
                                Locale.ENGLISH
                        )
        );
    }


    // =========================================================
    // MP VALIDATION
    // =========================================================

    private void validateMadhyaPradesh(
            LocationData location) {

        String resolved =
                location.getDisplayName();


        if (
                resolved == null
                        ||
                        !resolved
                                .toLowerCase(
                                        Locale.ROOT
                                )
                                .contains(
                                        "madhya pradesh"
                                )
        ) {

            throw new IllegalArgumentException(
                    "This seasonal planner currently covers Madhya Pradesh only."
            );
        }
    }


    // =========================================================
    // GENERAL HELPERS
    // =========================================================

    private String formatAllWindows(
            List<SowingWindow> windows) {

        return windows
                .stream()
                .map(
                        SowingWindow::getLabel
                )
                .reduce(
                        (a, b) ->
                                a + " | " + b
                )
                .orElse(
                        "Not configured"
                );
    }


    private String primarySeason(
            CropCalendarEntry crop) {

        return crop
                .getSowingWindows()
                .isEmpty()
                ? "Seasonal"
                : crop
                .getSowingWindows()
                .get(0)
                .getSeason();
    }


    private Map<String, Object> mapOf(
            Object... values) {

        Map<String, Object> map =
                new LinkedHashMap<>();


        for (
                int i = 0;
                i + 1 < values.length;
                i += 2
        ) {

            map.put(
                    String.valueOf(
                            values[i]
                    ),
                    values[i + 1]
            );
        }


        return map;
    }


    private int toInteger(
            Object value) {

        if (
                value instanceof Number number
        ) {

            return number.intValue();
        }


        try {

            return Integer.parseInt(
                    String.valueOf(
                            value
                    )
            );

        } catch (Exception ignored) {

            return 0;
        }
    }


    private Double toDouble(
            Object value,
            Double fallback) {

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

                return fallback;
            }
        }


        return fallback;
    }


    private double profitValue(
            Object value,
            double fallback) {

        if (
                value instanceof Number number
        ) {

            return number.doubleValue();
        }


        try {

            return Double.parseDouble(
                    String.valueOf(
                            value
                    )
            );

        } catch (Exception ignored) {

            return fallback;
        }
    }


    private double round(
            double value) {

        return Math.round(
                value
                        * 100.0
        )
                / 100.0;
    }
}