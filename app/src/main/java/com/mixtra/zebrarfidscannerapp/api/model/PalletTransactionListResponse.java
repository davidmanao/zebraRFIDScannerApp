package com.mixtra.zebrarfidscannerapp.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PalletTransactionListResponse {
    @SerializedName("data")
    private List<PalletTransactionItem> data;
    
    @SerializedName("total")
    private int total;
    
    @SerializedName("page")
    private int page;

    // Getters and Setters
    public List<PalletTransactionItem> getData() {
        return data;
    }

    public void setData(List<PalletTransactionItem> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public static class PalletTransactionItem {
        @SerializedName("id")
        private int id;
        
        @SerializedName("palletId")
        private int palletId;
        
        @SerializedName("locationId")
        private Integer locationId;
        
        @SerializedName("warehouseId")
        private Integer warehouseId;
        
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
        
        @SerializedName("dateIn")
        private String dateIn;
        
        @SerializedName("dateUp")
        private String dateUp;
        
        @SerializedName("palletTransactionDetails")
        private Object palletTransactionDetails;
        
        @SerializedName("location")
        private Object location;
        
        @SerializedName("warehouse")
        private Object warehouse;

        @SerializedName("pallet")
        private Pallet pallet;

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

        public Integer getLocationId() {
            return locationId;
        }

        public void setLocationId(Integer locationId) {
            this.locationId = locationId;
        }

        public Integer getWarehouseId() {
            return warehouseId;
        }

        public void setWarehouseId(Integer warehouseId) {
            this.warehouseId = warehouseId;
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

        public Object getPalletTransactionDetails() {
            return palletTransactionDetails;
        }

        public void setPalletTransactionDetails(Object palletTransactionDetails) {
            this.palletTransactionDetails = palletTransactionDetails;
        }

        public Object getLocation() {
            return location;
        }

        public void setLocation(Object location) {
            this.location = location;
        }

        public Object getWarehouse() {
            return warehouse;
        }

        public void setWarehouse(Object warehouse) {
            this.warehouse = warehouse;
        }

        public Pallet getPallet() {
            return pallet;
        }

        public void setPallet(Pallet pallet) {
            this.pallet = pallet;
        }

        // Convenience method to get pallet code
        public String getPalletCode() {
            return pallet != null ? pallet.getCode() : null;
        }
    }

    // Pallet class to represent the nested pallet object
    public static class Pallet {
        @SerializedName("id")
        private int id;
        
        @SerializedName("code")
        private String code;
        
        @SerializedName("status")
        private String status;
        
        @SerializedName("type")
        private String type;
        
        @SerializedName("createdAt")
        private String createdAt;
        
        @SerializedName("updatedAt")
        private String updatedAt;

        // Getters and Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}