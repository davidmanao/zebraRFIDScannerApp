package com.mixtra.zebrarfidscannerapp.api.model;

import com.google.gson.annotations.SerializedName;

public class PalletUpdateRequest {

    @SerializedName("id")
    private int id;

    @SerializedName("rfidTag")
    private String rfidTag;
    
    public PalletUpdateRequest(int id,String rfidTag) {
        this.rfidTag = rfidTag;
        this.id = id;
    }
    
    // Getters and setters
    public String getRfidTag() {
        return rfidTag;
    }
    
    public void setRfidTag(String rfidTag) {
        this.rfidTag = rfidTag;
    }
}