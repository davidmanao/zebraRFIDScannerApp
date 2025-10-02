package com.mixtra.zebrarfidscannerapp.api;

import android.content.Context;
import android.util.Log;

import com.mixtra.zebrarfidscannerapp.api.model.ApiResponse;
import com.mixtra.zebrarfidscannerapp.api.model.RfidTag;
import com.mixtra.zebrarfidscannerapp.api.model.TagInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RfidApiManager {
    private static final String TAG = "RfidApiManager";
    
    private RfidApiService apiService;
    private Context context;
    private String deviceId;
    private String currentUser;
    private String currentLocation;

    // Callback interfaces
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    public interface TagScanCallback {
        void onSuccess(RfidTag savedTag);
        void onError(String error);
    }
    
    public interface TagInfoCallback {
        void onSuccess(TagInfo tagInfo);
        void onError(String error);
    }
    
    public interface BatchScanCallback {
        void onSuccess(List<RfidTag> savedTags);
        void onError(String error);
    }
    
    public RfidApiManager(Context context) {
        this.context = context;
        this.apiService = ApiClient.getInstance(context).getApiService();
        this.deviceId = android.provider.Settings.Secure.getString(
                context.getContentResolver(), 
                android.provider.Settings.Secure.ANDROID_ID);
        this.currentUser = "Scanner_User"; // Default user
        this.currentLocation = "WAREHOUSE"; // Default location
    }
    
    // Getter for API service
    public RfidApiService getApiService() {
        return apiService;
    }
    
    // ========================
    // SINGLE TAG OPERATIONS
    // ========================
    
    /**
     * Submit a single scanned RFID tag to the API
     */
    public void submitTagScan(String tagId, int rssi, String scanType, TagScanCallback callback) {
        // Check if we're using the default placeholder URL
        String baseUrl = ApiClient.getInstance(context).getBaseUrl();
        if (baseUrl.contains("your-api-server.com")) {
            Log.w(TAG, "Skipping API submission - using placeholder URL. Configure API endpoint first.");
            if (callback != null) {
                callback.onError("API not configured - using placeholder URL");
            }
            return;
        }
        
        RfidTag tag = new RfidTag(tagId, scanType, rssi);
        tag.setDeviceId(deviceId);
        tag.setScannedBy(currentUser);
        tag.setLocation(currentLocation);
        
        Log.d(TAG, "Submitting tag scan: " + tagId + " (Type: " + scanType + ")");
        
        Call<ApiResponse<RfidTag>> call = apiService.submitTagScan(tag);
        call.enqueue(new Callback<ApiResponse<RfidTag>>() {
            @Override
            public void onResponse(Call<ApiResponse<RfidTag>> call, Response<ApiResponse<RfidTag>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<RfidTag> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Tag scan submitted successfully: " + tagId);
                        if (callback != null) {
                            callback.onSuccess(apiResponse.getData());
                        }
                    } else {
                        Log.e(TAG, "API error submitting tag scan: " + apiResponse.getMessage());
                        if (callback != null) {
                            callback.onError(apiResponse.getMessage());
                        }
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "HTTP error submitting tag scan: " + error);
                    if (callback != null) {
                        callback.onError(error);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<RfidTag>> call, Throwable t) {
                Log.e(TAG, "Network error submitting tag scan: " + t.getMessage());
                if (callback != null) {
                    callback.onError("Network error: " + t.getMessage());
                }
            }
        });
    }
    
    /**
     * Get detailed information about a tag from the API
     */
    public void getTagInfo(String tagId, TagInfoCallback callback) {
        // Check if we're using the default placeholder URL
        String baseUrl = ApiClient.getInstance(context).getBaseUrl();
        if (baseUrl.contains("your-api-server.com")) {
            Log.w(TAG, "Skipping API call - using placeholder URL. Configure API endpoint first.");
            if (callback != null) {
                callback.onError("API not configured - using placeholder URL");
            }
            return;
        }
        
        Log.d(TAG, "Getting tag info for: " + tagId);
        
        Call<ApiResponse<TagInfo>> call = apiService.getTagInfo(tagId);
        call.enqueue(new Callback<ApiResponse<TagInfo>>() {
            @Override
            public void onResponse(Call<ApiResponse<TagInfo>> call, Response<ApiResponse<TagInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<TagInfo> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Tag info retrieved successfully: " + tagId);
                        if (callback != null) {
                            callback.onSuccess(apiResponse.getData());
                        }
                    } else {
                        Log.w(TAG, "Tag info not found: " + apiResponse.getMessage());
                        if (callback != null) {
                            callback.onError(apiResponse.getMessage());
                        }
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "HTTP error getting tag info: " + error);
                    if (callback != null) {
                        callback.onError(error);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<TagInfo>> call, Throwable t) {
                Log.e(TAG, "Network error getting tag info: " + t.getMessage());
                if (callback != null) {
                    callback.onError("Network error: " + t.getMessage());
                }
            }
        });
    }
    
    // ========================
    // CONFIGURATION METHODS
    // ========================
    
    public void setCurrentUser(String user) {
        this.currentUser = user;
        Log.d(TAG, "Current user set to: " + user);
    }
    
    public void setCurrentLocation(String location) {
        this.currentLocation = location;
        Log.d(TAG, "Current location set to: " + location);
    }
    
    public String getCurrentUser() {
        return currentUser;
    }
    
    public String getCurrentLocation() {
        return currentLocation;
    }
    
    public String getDeviceId() {
        return deviceId;
    }

}