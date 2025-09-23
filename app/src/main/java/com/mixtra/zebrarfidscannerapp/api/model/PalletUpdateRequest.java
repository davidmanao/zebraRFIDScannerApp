package com.mixtra.zebrarfidscannerapp.api.model;

import com.google.gson.annotations.SerializedName;

public class PalletUpdateRequest {
    
    @SerializedName("rfidTag")
    private String rfidTag;
    
    public PalletUpdateRequest(String rfidTag) {
        this.rfidTag = rfidTag;
    }
    
    // Getters and setters
    public String getRfidTag() {
        return rfidTag;
    }
    
    public void setRfidTag(String rfidTag) {
        this.rfidTag = rfidTag;
    }
}