package com.mixtra.zebrarfidscannerapp.api.model;

import com.google.gson.annotations.SerializedName;

public class PalletDetailResponse {
    
    @SerializedName("data")
    private PalletDetail data;
    
    @SerializedName("resultMsg")
    private String resultMsg;
    
    // Getters and setters
    public PalletDetail getData() {
        return data;
    }
    
    public void setData(PalletDetail data) {
        this.data = data;
    }
    
    public String getResultMsg() {
        return resultMsg;
    }
    
    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }
    
    public static class PalletDetail {
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
        
        // Getters and setters
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
}