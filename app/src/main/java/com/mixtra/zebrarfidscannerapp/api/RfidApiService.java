package com.mixtra.zebrarfidscannerapp.api;

import com.mixtra.zebrarfidscannerapp.api.model.ApiResponse;
import com.mixtra.zebrarfidscannerapp.api.model.LoginRequest;
import com.mixtra.zebrarfidscannerapp.api.model.LoginResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletDetailResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletListResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletListResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletUpdateRequest;
import com.mixtra.zebrarfidscannerapp.api.model.RfidTag;
import com.mixtra.zebrarfidscannerapp.api.model.TagInfo;
import com.mixtra.zebrarfidscannerapp.api.models.PalletMaster;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface RfidApiService {
    
    // ========================
    // AUTHENTICATION
    // ========================
    
    /**
     * User login authentication
     * POST /auth/login
     */
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
    
    // ========================
    // TAG SCANNING OPERATIONS
    // ========================
    
    /**
     * Submit a single scanned tag
     * POST /api/rfid/tags/scan
     */
    @POST("api/rfid/tags/scan")
    Call<ApiResponse<RfidTag>> submitTagScan(@Body RfidTag tag);
    
    /**
     * Submit multiple scanned tags in batch
     * POST /api/rfid/tags/batch-scan
     */
    @POST("api/rfid/tags/batch-scan")
    Call<ApiResponse<List<RfidTag>>> submitBatchTagScan(@Body List<RfidTag> tags);
    
    // ========================
    // TAG INFORMATION
    // ========================
    
    /**
     * Get detailed information about a specific tag
     * GET /api/rfid/tags/{tagId}/info
     */
    @GET("api/rfid/tags/{tagId}/info")
    Call<ApiResponse<TagInfo>> getTagInfo(@Path("tagId") String tagId);
    
    /**
     * Search for tags by various criteria
     * GET /api/rfid/tags/search
     */
    @GET("api/rfid/tags/search")
    Call<ApiResponse<List<TagInfo>>> searchTags(@QueryMap Map<String, String> searchParams);
    
    // ========================
    // INVENTORY OPERATIONS
    // ========================
    
    /**
     * Start an inventory session
     * POST /api/rfid/inventory/start
     */
    @POST("api/rfid/inventory/start")
    Call<ApiResponse<Map<String, Object>>> startInventorySession(@Body Map<String, Object> sessionData);
    
    /**
     * Submit inventory results
     * POST /api/rfid/inventory/submit
     */
    @POST("api/rfid/inventory/submit")
    Call<ApiResponse<Map<String, Object>>> submitInventoryResults(@Body Map<String, Object> inventoryData);
    
    /**
     * Get inventory status
     * GET /api/rfid/inventory/status/{sessionId}
     */
    @GET("api/rfid/inventory/status/{sessionId}")
    Call<ApiResponse<Map<String, Object>>> getInventoryStatus(@Path("sessionId") String sessionId);
    
    // ========================
    // LOCATION & MOVEMENT
    // ========================
    
    /**
     * Update tag location
     * PUT /api/rfid/tags/{tagId}/location
     */
    @PUT("api/rfid/tags/{tagId}/location")
    Call<ApiResponse<RfidTag>> updateTagLocation(@Path("tagId") String tagId, @Body Map<String, String> locationData);
    
    /**
     * Get tags by location
     * GET /api/rfid/locations/{locationId}/tags
     */
    @GET("api/rfid/locations/{locationId}/tags")
    Call<ApiResponse<List<TagInfo>>> getTagsByLocation(@Path("locationId") String locationId);
    
    // ========================
    // REPORTS & ANALYTICS
    // ========================
    
    /**
     * Get scan history for a specific tag
     * GET /api/rfid/tags/{tagId}/history
     */
    @GET("api/rfid/tags/{tagId}/history")
    Call<ApiResponse<List<RfidTag>>> getTagScanHistory(@Path("tagId") String tagId);
    
    /**
     * Get scanning statistics
     * GET /api/rfid/reports/stats
     */
    @GET("api/rfid/reports/stats")
    Call<ApiResponse<Map<String, Object>>> getScanningStats(@QueryMap Map<String, String> dateRange);
    
    /**
     * Export scan data
     * GET /api/rfid/export
     */
    @GET("api/rfid/export")
    Call<ApiResponse<String>> exportScanData(@QueryMap Map<String, String> exportParams);
    
    // ========================
    // SYNC OPERATIONS
    // ========================
    
    /**
     * Sync device data with server
     * POST /api/rfid/sync
     */
    @POST("api/rfid/sync")
    Call<ApiResponse<Map<String, Object>>> syncDeviceData(@Body Map<String, Object> syncData);
    
    /**
     * Get sync status
     * GET /api/rfid/sync/status
     */
    @GET("api/rfid/sync/status")
    Call<ApiResponse<Map<String, Object>>> getSyncStatus(@Query("device_id") String deviceId);
    
    // ========================
    // PALLET MASTER OPERATIONS
    // ========================
    
    /**
     * Get list of all pallets
     * GET /Pallet?limit=0&page=0
     */
    @GET("Pallet")
    Call<PalletListResponse> getPalletList(@Query("limit") int limit, @Query("page") int page);
    
    /**
     * Get filtered list of pallets
     * GET /Pallet?filter=code%3AP-SAP&limit=20&page=0
     */
    @GET("Pallet")
    Call<PalletListResponse> getPalletList(@Query("filter") String filter, @Query("limit") int limit, @Query("page") int page);

    /**
     * Get pallet detail by ID
     * GET /Pallet/{id}
     */
    @GET("Pallet/{id}")
    Call<PalletDetailResponse> getPalletDetail(@Path("id") int palletId);

    /**
     * Update pallet RFID tag
     * PUT /Pallet/{id}
     */
    @PUT("Pallet/{id}")
    Call<PalletDetailResponse> updatePalletRfidTag(@Path("id") int palletId, @Body PalletUpdateRequest updateRequest);
    
    /**
     * Submit a completed pallet with RFID tags
     * POST /api/pallet/master
     */
    @POST("api/pallet/master")
    Call<ApiResponse<PalletMaster>> submitPalletMaster(@Body PalletMaster pallet);
    
    /**
     * Get pallet master data by pallet ID
     * GET /api/pallet/master/{palletId}
     */
    @GET("api/pallet/master/{palletId}")
    Call<ApiResponse<PalletMaster>> getPalletMaster(@Path("palletId") String palletId);
    
    /**
     * Update pallet data
     * PUT /Pallet/{id}
     */
    @PUT("Pallet/rfid")
    Call<ApiResponse<PalletMaster>> updatePallet(@Body PalletMaster pallet);
    
    /**
     * Get pallet data by ID
     * GET /Pallet/{id}
     */
    @GET("Pallet/{id}")
    Call<PalletListResponse.SinglePalletResponse> getPallet(@Path("id") int palletId);

    /**
     * Get all pallets for a location
     * GET /api/pallet/master
     */
    @GET("api/pallet/master")
    Call<ApiResponse<List<PalletMaster>>> getPallets(@Query("location") String location, 
                                                     @Query("status") String status,
                                                     @Query("created_by") String createdBy);
    
    /**
     * Verify RFID tags exist in pallet
     * POST /api/pallet/verify
     */
    @POST("api/pallet/verify")
    Call<ApiResponse<Map<String, Object>>> verifyPalletTags(@Body Map<String, Object> verificationData);
    
    /**
     * Get pallet by code (for barcode scan)
     * GET /Pallet/code/{code}
     */
    @GET("Pallet/code/{code}")
    Call<PalletListResponse.SinglePalletResponse> getPalletByCode(@Path("code") String code);
}