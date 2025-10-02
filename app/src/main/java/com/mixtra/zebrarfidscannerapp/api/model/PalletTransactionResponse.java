package com.mixtra.zebrarfidscannerapp.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PalletTransactionResponse {
    @SerializedName("data")
    private TransactionData data;
    
    @SerializedName("resultMsg")
    private String resultMsg;

    // Getters and Setters
    public TransactionData getData() {
        return data;
    }

    public void setData(TransactionData data) {
        this.data = data;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }
    
    // For backward compatibility - delegate to data object
    public int getId() {
        return data != null ? data.getId() : 0;
    }

    public static class TransactionData {
        @SerializedName("id")
        private int id;
        
        @SerializedName("palletId")
        private int palletId;
        
        @SerializedName("locationId")
        private Integer locationId;
        
        @SerializedName("warehouseId")
        private Integer warehouseId;

        @SerializedName("warehouseText")
        private String WarehouseText;

        @SerializedName("locationText")
        private String LocationText;
        
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
        
        @SerializedName("userIn")
        private String userIn;
        
        @SerializedName("userUp")
        private String userUp;
        
        @SerializedName("palletTransactionDetails")
        private List<TransactionDetail> palletTransactionDetails;
        
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

        public String getWarehouseText() {
            return WarehouseText;
        }
        public void setWarehouseText(String warehouseText) {
            WarehouseText = warehouseText;
        }

        public String getLocationText() {
            return LocationText;
        }
        public void setLocationText(String locationText) {
            LocationText = locationText;
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

        public String getUserIn() {
            return userIn;
        }

        public void setUserIn(String userIn) {
            this.userIn = userIn;
        }

        public String getUserUp() {
            return userUp;
        }

        public void setUserUp(String userUp) {
            this.userUp = userUp;
        }

        public List<TransactionDetail> getPalletTransactionDetails() {
            return palletTransactionDetails;
        }

        public void setPalletTransactionDetails(List<TransactionDetail> palletTransactionDetails) {
            this.palletTransactionDetails = palletTransactionDetails;
        }

        public Location getLocation() {
            return (Location) location;
        }

        public void setLocation(Object location) {
            this.location = location;
        }

        public Warehouse getWarehouse() {
            return (Warehouse) warehouse;
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
    }

    public static class Pallet {
        @SerializedName("id")
        private int id;
        
        @SerializedName("code")
        private String code;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("type")
        private String type;
        
        @SerializedName("capacity")
        private double capacity;
        
        @SerializedName("tare")
        private double tare;
        
        @SerializedName("initial")
        private double initial;
        
        @SerializedName("incoming")
        private double incoming;
        
        @SerializedName("outgoing")
        private double outgoing;
        
        @SerializedName("balance")
        private double balance;
        
        @SerializedName("isUnique")
        private boolean isUnique;
        
        @SerializedName("dateIn")
        private String dateIn;
        
        @SerializedName("dateUp")
        private String dateUp;
        
        @SerializedName("userIn")
        private String userIn;
        
        @SerializedName("userUp")
        private String userUp;
        
        @SerializedName("rfidTag")
        private String rfidTag;

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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getCapacity() {
            return capacity;
        }

        public void setCapacity(double capacity) {
            this.capacity = capacity;
        }

        public double getTare() {
            return tare;
        }

        public void setTare(double tare) {
            this.tare = tare;
        }

        public double getInitial() {
            return initial;
        }

        public void setInitial(double initial) {
            this.initial = initial;
        }

        public double getIncoming() {
            return incoming;
        }

        public void setIncoming(double incoming) {
            this.incoming = incoming;
        }

        public double getOutgoing() {
            return outgoing;
        }

        public void setOutgoing(double outgoing) {
            this.outgoing = outgoing;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }

        public boolean isUnique() {
            return isUnique;
        }

        public void setUnique(boolean unique) {
            isUnique = unique;
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

        public String getUserIn() {
            return userIn;
        }

        public void setUserIn(String userIn) {
            this.userIn = userIn;
        }

        public String getUserUp() {
            return userUp;
        }

        public void setUserUp(String userUp) {
            this.userUp = userUp;
        }

        public String getRfidTag() {
            return rfidTag;
        }

        public void setRfidTag(String rfidTag) {
            this.rfidTag = rfidTag;
        }
    }

    public static class Location{
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;
        @SerializedName("code")
        private String code;
        @SerializedName("remark")
        private String remark;

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

    }

    public static  class Warehouse{
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;
        @SerializedName("code")
        private String code;
        @SerializedName("remark")
        private String remark;

        public String getCode() {
            return code;
        }
        public String getName() {
            return name;
        }
        public int getId() {
            return id;
        }
    }
    // TransactionDetail class for API response
    public static class TransactionDetail {
        @SerializedName("id")
        private int id;
        
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
        
        @SerializedName("userIn")
        private String userIn;
        
        @SerializedName("userUp")
        private String userUp;

        // Getters and Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

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

        public String getUserIn() {
            return userIn;
        }

        public void setUserIn(String userIn) {
            this.userIn = userIn;
        }

        public String getUserUp() {
            return userUp;
        }

        public void setUserUp(String userUp) {
            this.userUp = userUp;
        }
    }

    // Legacy BatchItem class for backward compatibility
    public static class BatchItem {
        private String batchNo;
        private String productCode;
        private String productName;
        private double quantity;

        public String getBatchNo() {
            return batchNo;
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }
    }
}