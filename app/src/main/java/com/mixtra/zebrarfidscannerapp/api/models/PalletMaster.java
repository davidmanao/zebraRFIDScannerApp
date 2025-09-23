package com.mixtra.zebrarfidscannerapp.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;


public class PalletMaster {
    
    @SerializedName("id")
    private Integer id;
    @SerializedName("code")
    private String code;
    @SerializedName("name")
    private String name;
    
    @SerializedName("rfidTag")
    private String rfidTag;
    
    @SerializedName("dateUp")
    private Date dateUp;

    @SerializedName("dateIn")
    private Date dateIn;
    @SerializedName("userIn")
    private String userIn;

    @SerializedName("userIn")
    private String userInBy;
    @SerializedName("userUp")
    private String userUp;

    @SerializedName("userUp")
    private String userUpBy;

    @SerializedName("type")
    private String type;

    
    @SerializedName("capacity")
    private Float capacity;
    @SerializedName("tare")
    private Float tare;
    
    @SerializedName("initial")
    private Float initial;
    @SerializedName("incoming")
    private Float incoming;
    @SerializedName("outgoing")
    private Float outgoing;
    @SerializedName("balance")
    private Float balance;

    @SerializedName("isUnique")
    private Boolean isUnique;
    

    
    public PalletMaster(Integer palletId, String rfidTag) {
        this.id = palletId;
        this.rfidTag = rfidTag;
    }

    // Getters and Setters

    public String getUserUp() {
        return userUp;
    }
    public void setUserUp(Integer userUp) {
        this.userUp = userUp.toString();
    }
    public String getUserUpBy() {
        return userUpBy;
    }

    public Integer getPalletId() {
        return id;
    }
    public void setPalletId(Integer palletId) {
        this.id = palletId;
    }
    public String getPalletCode() {
        return code;
    }
    public void setPalletCode(String palletCode) {
        this.code = palletCode;
    }
    public String getPalletName() {
        return name;
    }

    public void setPalletName(String palletName) {
        this.name = palletName;
    }
    
    public String getRfidTag() {
        return rfidTag;
    }
    public void setRfidTag(String rfidTag) {
        this.rfidTag = rfidTag;
    }

    public Date getCreatedDate() {
        return dateIn;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.dateIn = createdDate;
    }
    
    public String getCreatedBy() {
        return "Admin";
    }
    
    public void setCreatedBy(String createdBy) {
        this.userUp = createdBy;
    }
    


    public String getType() { return type; }

    public Float getCapacity() {
        return capacity;
    }
    public Float getTare() {
        return tare;
    }
    public Float getInitial() {
        return initial;
    }
    public Float getIncoming() {
        return incoming;
    }
    public Float getBalance() {
        return balance;
    }
    public Boolean getIsUnique() {
        return isUnique;
    }


    @Override
    public String toString() {
        return "PalletMaster{" +
                "palletId='" + id + '\'' +
                ", rfidTag=" + rfidTag +
                ", createdBy='" + userInBy + '\'' +
                ", createdDate=" + dateIn +
                '}';
    }
}