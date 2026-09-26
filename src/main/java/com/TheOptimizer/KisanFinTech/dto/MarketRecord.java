package com.TheOptimizer.KisanFinTech.dto;

public class MarketRecord {

    private String month;
    private String commodityName;
    private Double avgModalPrice;
    private Double avgMinPrice;
    private Double avgMaxPrice;
    private Double change;
    private Double confidence;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }

    public Double getAvgModalPrice() {
        return avgModalPrice;
    }

    public void setAvgModalPrice(Double avgModalPrice) {
        this.avgModalPrice = avgModalPrice;
    }

    public Double getAvgMinPrice() {
        return avgMinPrice;
    }

    public void setAvgMinPrice(Double avgMinPrice) {
        this.avgMinPrice = avgMinPrice;
    }

    public Double getAvgMaxPrice() {
        return avgMaxPrice;
    }

    public void setAvgMaxPrice(Double avgMaxPrice) {
        this.avgMaxPrice = avgMaxPrice;
    }

    public Double getChange() {
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}