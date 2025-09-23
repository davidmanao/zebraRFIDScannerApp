package com.mixtra.zebrarfidscannerapp.api.model;


import com.google.gson.annotations.SerializedName;

public class RfidTag {
    @SerializedName("tag_id")
    private String tagId;
    
    @SerializedName("item_code")
    private String itemCode;
    
    @SerializedName("product_name")
    private String productName;
    
    @SerializedName("location")
    private String location;
    
    @SerializedName("quantity")
    private Integer quantity;
    
    @SerializedName("batch_number")
    private String batchNumber;
    
    @SerializedName("scan_timestamp")
    private String scanTimestamp;
    
    @SerializedName("scanned_by")
    private String scannedBy;
    
    @SerializedName("scan_type")
    private String scanType; // "IN", "OUT", "INVENTORY", "TRANSFER"
    
    @SerializedName("rssi")
    private Integer rssi;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("notes")
    private String notes;
    
    @SerializedName("device_id")
    private String deviceId;
    
    // Constructors
    public RfidTag() {}
    
    public RfidTag(String tagId, String scanType, Integer rssi) {
        this.tagId = tagId;
        this.scanType = scanType;
        this.rssi = rssi;
        this.scanTimestamp = getCurrentTimestamp();
        this.status = "ACTIVE";
    }
    
    private String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }
    
    // Getters and Setters
    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
    
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    
    public String getScanTimestamp() { return scanTimestamp; }
    public void setScanTimestamp(String scanTimestamp) { this.scanTimestamp = scanTimestamp; }
    
    public String getScannedBy() { return scannedBy; }
    public void setScannedBy(String scannedBy) { this.scannedBy = scannedBy; }
    
    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }
    
    public Integer getRssi() { return rssi; }
    public void setRssi(Integer rssi) { this.rssi = rssi; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}