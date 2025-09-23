package com.mixtra.zebrarfidscannerapp.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("resultMsg")
    private String resultMsg;
    
    @SerializedName("data")
    private T data;
    
    @SerializedName("error_code")
    private String errorCode;
    
    @SerializedName("timestamp")
    private String timestamp;
    
    // Constructors
    public ApiResponse() {}
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getResultMsg() { return resultMsg; }
    public void setResultMsg(String resultMsg) { this.resultMsg = resultMsg; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", resultMsg='" + resultMsg + '\'' +
                ", data=" + (data != null ? "present" : "null") +
                ", errorCode='" + errorCode + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}