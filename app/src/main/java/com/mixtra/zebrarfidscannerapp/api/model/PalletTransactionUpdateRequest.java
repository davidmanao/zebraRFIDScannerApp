package com.mixtra.zebrarfidscannerapp.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PalletTransactionUpdateRequest {
    @SerializedName("id")
    private int id;
    
    @SerializedName("palletId")
    private int palletId;
    
    @SerializedName("transactionDate")
    private String transactionDate;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("transactionType")
    private String transactionType;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("remark")
    private String remark;
    
    @SerializedName("warehouseCode")
    private String warehouseCode;
    
    @SerializedName("warehouseText")
    private String warehouseText;

    @SerializedName("locationText")
    private String locationText;
    
    @SerializedName("locationCode")
    private String locationCode;
    
    @SerializedName("palletTransactionDetails")
    private List<PalletTransactionDetail> palletTransactionDetails;

    // Constructor
    public PalletTransactionUpdateRequest() {
    }

    public PalletTransactionUpdateRequest(int id, int palletId, String transactionDate, 
                                        String status, String transactionType, String type, 
                                        String remark, String warehouseCode, String locationCode, 
                                        List<PalletTransactionDetail> details) {
        this.id = id;
        this.palletId = palletId;
        this.transactionDate = transactionDate;
        this.status = status;
        this.transactionType = transactionType;
        this.type = type;
        this.remark = remark;
        this.warehouseText = warehouseCode;
        this.locationText = locationCode;
        this.palletTransactionDetails = details;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public List<PalletTransactionDetail> getPalletTransactionDetails() {
        return palletTransactionDetails;
    }

    public void setPalletTransactionDetails(List<PalletTransactionDetail> palletTransactionDetails) {
        this.palletTransactionDetails = palletTransactionDetails;
    }

    public static class PalletTransactionDetail {
        @SerializedName("palletTransactionId")
        private int palletTransactionId;
        
        @SerializedName("batchNo")
        private String batchNo;
        
        @SerializedName("qty")
        private String qty;
        
        @SerializedName("remark")
        private String remark;
        
        @SerializedName("dateIn")
        private String dateIn;
        
        @SerializedName("dateUp")
        private String dateUp;

        // Constructor
        public PalletTransactionDetail() {
        }

        public PalletTransactionDetail(int palletTransactionId, String batchNo, String qty, 
                                     String remark, String dateIn, String dateUp) {
            this.palletTransactionId = palletTransactionId;
            this.batchNo = batchNo;
            this.qty = qty;
            this.remark = remark;
            this.dateIn = dateIn;
            this.dateUp = dateUp;
        }

        // Getters and Setters
        public int getPalletTransactionId() {
            return palletTransactionId;
        }

        public void setPalletTransactionId(int palletTransactionId) {
            this.palletTransactionId = palletTransactionId;
        }

        public String getBatchNo() {
            return batchNo;
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }

        public String getQty() {
            return qty;
        }

        public void setQty(String qty) {
            this.qty = qty;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getDateIn() {
            return dateIn;
        }

        public void setDateIn(String dateIn) {
            this.dateIn = dateIn;
        }

        public String getDateUp() {
            return dateUp;
        }

        public void setDateUp(String dateUp) {
            this.dateUp = dateUp;
        }
    }
}