package com.mixtra.zebrarfidscannerapp.api.model;

import com.google.gson.annotations.SerializedName;

public class PalletTransactionRequest {
    @SerializedName("palletId")
    private int palletId;
    
    @SerializedName("transactionDate")
    private String transactionDate;
    
    @SerializedName("transactionType")
    private String transactionType;
    
    @SerializedName("type")
    private String type;

    // Constructor
    public PalletTransactionRequest(int palletId, String transactionDate, String transactionType, String type) {
        this.palletId = palletId;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.type = type;
    }

    // Getters and Setters
    public int getPalletId() {
        return palletId;
    }

    public void setPalletId(int palletId) {
        this.palletId = palletId;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}