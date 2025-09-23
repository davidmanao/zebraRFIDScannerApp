package com.mixtra.zebrarfidscannerapp.api.model;

import java.util.List;

public class PalletListResponse {
    private List<PalletData> data;
    private int total;
    private int page;
    private int limit;
    private boolean success;
    private String message;

    public List<PalletData> getData() {
        return data;
    }

    public void setData(List<PalletData> data) {
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

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class PalletData {
        private Integer id;
        private String code;
        private String name;
        private String type;
        private Integer capacity;
        private Integer tare;
        private Integer initial;
        private Integer incoming;
        private Integer outgoing;
        private Integer balance;
        private Boolean isUnique;
        private String dateIn;
        private String dateUp;
        private String userIn;
        private String userUp;
        private String rfidTag;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
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

        public Integer getCapacity() {
            return capacity;
        }

        public void setCapacity(Integer capacity) {
            this.capacity = capacity;
        }

        public Integer getTare() {
            return tare;
        }

        public void setTare(Integer tare) {
            this.tare = tare;
        }

        public Integer getInitial() {
            return initial;
        }

        public void setInitial(Integer initial) {
            this.initial = initial;
        }

        public Integer getIncoming() {
            return incoming;
        }

        public void setIncoming(Integer incoming) {
            this.incoming = incoming;
        }

        public Integer getOutgoing() {
            return outgoing;
        }

        public void setOutgoing(Integer outgoing) {
            this.outgoing = outgoing;
        }

        public Integer getBalance() {
            return balance;
        }

        public void setBalance(Integer balance) {
            this.balance = balance;
        }

        public Boolean getIsUnique() {
            return isUnique;
        }

        public void setIsUnique(Boolean isUnique) {
            this.isUnique = isUnique;
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

    /**
     * Response wrapper for single pallet API calls (/Pallet/code/{code})
     */
    public static class SinglePalletResponse {
        private PalletData data;
        private String resultMsg;

        public PalletData getData() {
            return data;
        }

        public void setData(PalletData data) {
            this.data = data;
        }

        public String getResultMsg() {
            return resultMsg;
        }

        public void setResultMsg(String resultMsg) {
            this.resultMsg = resultMsg;
        }
    }
}