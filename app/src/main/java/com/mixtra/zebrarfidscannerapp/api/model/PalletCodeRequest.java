package com.mixtra.zebrarfidscannerapp.api.model;

import com.google.gson.annotations.SerializedName;

public class PalletCodeRequest {
    @SerializedName("code")
    private String code;

    public PalletCodeRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}