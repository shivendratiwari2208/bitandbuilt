package com.TheOptimizer.KisanFinTech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class SowingPlannerRequest {

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    @NotNull(message = "Farm area is required")
    @Positive(message = "Farm area must be greater than 0")
    private Double farmAreaAcres;

    @NotNull(message = "Sowing date is required")
    private LocalDate sowingDate;

    @NotNull(message = "Harvest date is required")
    private LocalDate harvestDate;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public Double getFarmAreaAcres() {
        return farmAreaAcres;
    }

    public void setFarmAreaAcres(Double farmAreaAcres) {
        this.farmAreaAcres = farmAreaAcres;
    }

    public LocalDate getSowingDate() {
        return sowingDate;
    }

    public void setSowingDate(LocalDate sowingDate) {
        this.sowingDate = sowingDate;
    }

    public LocalDate getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(LocalDate harvestDate) {
        this.harvestDate = harvestDate;
    }
}