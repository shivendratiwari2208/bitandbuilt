package com.TheOptimizer.KisanFinTech.dto;

public class FeatureData {

    private Double temperature;
    private Double humidity;
    private Double moisture;
    private String soilType;
    private Double phosphorous;

    public FeatureData() {
    }

    public FeatureData(
            Double temperature,
            Double humidity,
            Double moisture,
            String soilType,
            Double phosphorous
    ) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.moisture = moisture;
        this.soilType = soilType;
        this.phosphorous = phosphorous;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getMoisture() {
        return moisture;
    }

    public void setMoisture(Double moisture) {
        this.moisture = moisture;
    }

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }

    public Double getPhosphorous() {
        return phosphorous;
    }

    public void setPhosphorous(Double phosphorous) {
        this.phosphorous = phosphorous;
    }
}