package com.TheOptimizer.KisanFinTech.dto;

import java.util.ArrayList;
import java.util.List;

public class CropCalendarEntry {

    private String crop;
    private String category;
    private String marketGroup;

    private boolean statewideMajor;

    private int harvestDaysMin;
    private int harvestDaysMax;

    private double avgInvestmentPerHectare;
    private double avgYieldQuintalPerHectare;
    private double benchmarkPricePerQuintal;

    private List<String> soilTypes = new ArrayList<>();

    private double minTemperatureC;
    private double maxTemperatureC;

    private String source;
    private String note;

    private List<String> seasons = new ArrayList<>();

    private List<SowingWindow> sowingWindows =
            new ArrayList<>();

    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMarketGroup() {
        return marketGroup;
    }

    public void setMarketGroup(String marketGroup) {
        this.marketGroup = marketGroup;
    }

    public boolean isStatewideMajor() {
        return statewideMajor;
    }

    public void setStatewideMajor(boolean statewideMajor) {
        this.statewideMajor = statewideMajor;
    }

    public int getHarvestDaysMin() {
        return harvestDaysMin;
    }

    public void setHarvestDaysMin(int harvestDaysMin) {
        this.harvestDaysMin = harvestDaysMin;
    }

    public int getHarvestDaysMax() {
        return harvestDaysMax;
    }

    public void setHarvestDaysMax(int harvestDaysMax) {
        this.harvestDaysMax = harvestDaysMax;
    }

    public double getAvgInvestmentPerHectare() {
        return avgInvestmentPerHectare;
    }

    public void setAvgInvestmentPerHectare(
            double avgInvestmentPerHectare) {

        this.avgInvestmentPerHectare =
                avgInvestmentPerHectare;
    }

    public double getAvgYieldQuintalPerHectare() {
        return avgYieldQuintalPerHectare;
    }

    public void setAvgYieldQuintalPerHectare(
            double avgYieldQuintalPerHectare) {

        this.avgYieldQuintalPerHectare =
                avgYieldQuintalPerHectare;
    }

    public double getBenchmarkPricePerQuintal() {
        return benchmarkPricePerQuintal;
    }

    public void setBenchmarkPricePerQuintal(
            double benchmarkPricePerQuintal) {

        this.benchmarkPricePerQuintal =
                benchmarkPricePerQuintal;
    }

    public List<String> getSoilTypes() {
        return soilTypes;
    }

    public void setSoilTypes(
            List<String> soilTypes) {

        this.soilTypes = soilTypes;
    }

    public double getMinTemperatureC() {
        return minTemperatureC;
    }

    public void setMinTemperatureC(
            double minTemperatureC) {

        this.minTemperatureC =
                minTemperatureC;
    }

    public double getMaxTemperatureC() {
        return maxTemperatureC;
    }

    public void setMaxTemperatureC(
            double maxTemperatureC) {

        this.maxTemperatureC =
                maxTemperatureC;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<String> getSeasons() {
        return seasons;
    }

    public void setSeasons(
            List<String> seasons) {

        this.seasons = seasons;
    }

    public List<SowingWindow> getSowingWindows() {
        return sowingWindows;
    }

    public void setSowingWindows(
            List<SowingWindow> sowingWindows) {

        this.sowingWindows =
                sowingWindows;
    }
}