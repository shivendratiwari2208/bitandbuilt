package com.TheOptimizer.KisanFinTech.dto;

import jakarta.validation.constraints.NotBlank;

public class PesticideRequest {

    @NotBlank(message = "Crop is required")
    private String crop;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    public PesticideRequest() {
    }

    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

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
}