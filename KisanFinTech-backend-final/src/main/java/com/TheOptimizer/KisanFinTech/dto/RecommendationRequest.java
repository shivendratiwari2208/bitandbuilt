package com.TheOptimizer.KisanFinTech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class RecommendationRequest {

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    @NotNull(message = "Area is required")
    private Double area;

    @NotNull(message = "Sowing date is required")
    private LocalDate sowingDate;


    public String getLocation() {
        return location;
    }

    public void setLocation(
            String location
    ) {
        this.location = location;
    }


    public String getPincode() {
        return pincode;
    }

    public void setPincode(
            String pincode
    ) {
        this.pincode = pincode;
    }


    public Double getArea() {
        return area;
    }

    public void setArea(
            Double area
    ) {
        this.area = area;
    }


    public LocalDate getSowingDate() {
        return sowingDate;
    }

    public void setSowingDate(
            LocalDate sowingDate
    ) {
        this.sowingDate = sowingDate;
    }
}