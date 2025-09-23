package com.mixtra.zebrarfidscannerapp.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String PREF_NAME = "rfid_api_prefs";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_API_TOKEN = "api_token";
    
    // Default configuration - Mixtra API
    private static final String DEFAULT_BASE_URL = "https://ws-api-dev.mixtra.co.id/";
    private static final int CONNECT_TIMEOUT = 30;
    private static final int READ_TIMEOUT = 30;
    private static final int WRITE_TIMEOUT = 30;
    
    private static ApiClient instance;
    private RfidApiService apiService;
    private Context context;
    private SharedPreferences preferences;
    
    private ApiClient(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        initializeApiService();
    }
    
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }
    
    private void initializeApiService() {
        String baseUrl = getBaseUrl();
        
        // Create logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // Create auth interceptor for API token
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                
                String apiToken = getApiToken();
                if (apiToken != null && !apiToken.isEmpty()) {
                    Request.Builder builder = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + apiToken)
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json");
                    
                    Request newRequest = builder.build();
                    return chain.proceed(newRequest);
                } else {
                    // Add basic headers even without token
                    Request.Builder builder = originalRequest.newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json");
                    
                    Request newRequest = builder.build();
                    return chain.proceed(newRequest);
                }
            }
        };
        
        // Build OkHttp client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor);
        
        // Build Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(RfidApiService.class);
        
        Log.d(TAG, "API Client initialized with base URL: " + baseUrl);
    }
    
    public RfidApiService getApiService() {
        return apiService;
    }
    
    // Configuration methods
    public void setBaseUrl(String baseUrl) {
        if (baseUrl != null && !baseUrl.equals(getBaseUrl())) {
            preferences.edit().putString(KEY_BASE_URL, baseUrl).apply();
            initializeApiService(); // Reinitialize with new URL
            Log.d(TAG, "Base URL updated to: " + baseUrl);
        }
    }
    
    public String getBaseUrl() {
        return preferences.getString(KEY_BASE_URL, DEFAULT_BASE_URL);
    }
    
    public void setApiToken(String token) {
        preferences.edit().putString(KEY_API_TOKEN, token).apply();
        initializeApiService(); // Reinitialize with new token
        Log.d(TAG, "API token updated");
    }
    
    public String getApiToken() {
        return preferences.getString(KEY_API_TOKEN, "");
    }
    
    public boolean hasValidConfiguration() {
        String baseUrl = getBaseUrl();
        return baseUrl != null && !baseUrl.isEmpty() && !baseUrl.equals(DEFAULT_BASE_URL);
    }
    
    public void clearConfiguration() {
        preferences.edit().clear().apply();
        initializeApiService();
        Log.d(TAG, "API configuration cleared");
    }
}