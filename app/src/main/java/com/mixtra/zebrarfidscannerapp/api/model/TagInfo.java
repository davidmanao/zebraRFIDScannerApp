package com.mixtra.zebrarfidscannerapp.api.model;

import com.google.gson.annotations.SerializedName;

public class TagInfo {
    @SerializedName("tag_id")
    private String tagId;
    
    @SerializedName("item_code")
    private String itemCode;
    
    @SerializedName("product_name")
    private String productName;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("current_location")
    private String currentLocation;
    
    @SerializedName("expected_quantity")
    private Integer expectedQuantity;
    
    @SerializedName("last_seen")
    private String lastSeen;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("price")
    private Double price;
    
    @SerializedName("supplier")
    private String supplier;
    
    // Constructors
    public TagInfo() {}
    
    // Getters and Setters
    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
    
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
    
    public Integer getExpectedQuantity() { return expectedQuantity; }
    public void setExpectedQuantity(Integer expectedQuantity) { this.expectedQuantity = expectedQuantity; }
    
    public String getLastSeen() { return lastSeen; }
    public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
}