package com.mixtra.zebrarfidscannerapp.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.mixtra.zebrarfidscannerapp.MainActivity;
import com.mixtra.zebrarfidscannerapp.api.ApiClient;
import com.mixtra.zebrarfidscannerapp.api.RfidApiService;
import com.mixtra.zebrarfidscannerapp.api.model.LoginRequest;
import com.mixtra.zebrarfidscannerapp.api.model.LoginResponse;
import com.mixtra.zebrarfidscannerapp.databinding.ActivityLoginBinding;
import com.mixtra.zebrarfidscannerapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_ADMIN = "is_admin";

    // UI Components
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvStatus;

    // API
    private ApiClient apiClient;
    private RfidApiService apiService;
    private SharedPreferences authPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Initialize preferences
        authPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize API client
        apiClient = ApiClient.getInstance(this);
        apiService = apiClient.getApiService();

        // Initialize UI
        initializeViews();
        setupEventListeners();

        // Check if user is already logged in
        checkExistingSession();
    }

    private void initializeViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        tvStatus = findViewById(R.id.tv_status);
    }

    private void setupEventListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Allow login with Enter key
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            attemptLogin();
            return true;
        });
    }

    private void checkExistingSession() {
        String existingToken = authPrefs.getString(KEY_TOKEN, "");
        if (!TextUtils.isEmpty(existingToken)) {
            Log.d(TAG, "Found existing token, redirecting to main activity");
            updateStatus("Restoring session...");
            redirectToMainActivity();
        }
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }
        // In attemptLogin()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            etUsername.setError("Invalid email address");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Show loading state
        setLoadingState(true);
        updateStatus("Signing in...");

        // Create login request
        LoginRequest loginRequest = new LoginRequest(username, password);

        // Make API call
        Call<LoginResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                setLoadingState(false);
                try{
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse loginResponse = response.body();

                        if (!TextUtils.isEmpty(loginResponse.getToken())) {
                            Log.d(TAG, "Login successful");
                            handleLoginSuccess(loginResponse);
                        } else {
                            String errorMsg = loginResponse.getResultMsg() != null ?
                                    loginResponse.getResultMsg() : "Login failed - no token received";
                            handleLoginError(errorMsg);
                        }
                    } else if(response.errorBody() != null) {
                        String errorJson;
                        errorJson = response.errorBody().string();
                        // Parse errorJson to LoginResponse if needed
                        Gson gson = new Gson();
                        LoginResponse errorResponse = gson.fromJson(errorJson, LoginResponse.class);
                        String rspMsg = errorResponse.getResultMsg() != null ?
                                errorResponse.getResultMsg() : "Unknown error";
                        String errorMsg =  ": " + rspMsg;
                        handleLoginError(errorMsg);
                    }else {
                        String errorMsg = "Login failed - HTTP " + response.code();
                        errorMsg += ": " + response.message();
                        handleLoginError(errorMsg);
                    }
                } catch (Exception e) {
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    handleLoginError("Error: " + errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull  Call<LoginResponse> call, @NonNull Throwable t) {
                setLoadingState(false);
                Log.e(TAG, "Login API call failed", t);
                handleLoginError("Network error: " + t.getMessage());
            }
        });
    }

    private void handleLoginSuccess(LoginResponse response) {
        Log.d(TAG, "Processing successful login response");

        // Store authentication data in auth_prefs
        SharedPreferences.Editor authEditor = authPrefs.edit();
        authEditor.putString(KEY_TOKEN, response.getToken());

        if (response.getData() != null) {
            LoginResponse.UserData userData = response.getData();
            authEditor.putInt(KEY_USER_ID, userData.getId());
            authEditor.putString(KEY_USER_NAME, userData.getFullName());
            authEditor.putString(KEY_USER_EMAIL, userData.getEmail());
            authEditor.putBoolean(KEY_IS_ADMIN, userData.isAdmin());
        }

        authEditor.apply();

        // Store user info for MainActivity in login_prefs (to match HomeFragment)
        SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        SharedPreferences.Editor loginEditor = loginPrefs.edit();

        if (response.getData() != null) {
            LoginResponse.UserData userData = response.getData();
            loginEditor.putString("username", userData.getFullName());

            // Get organization name from first organization member if available
            String organizationName = "Unknown Organization";
            if (userData.getOrganizationMembers() != null && !userData.getOrganizationMembers().isEmpty()) {
                organizationName = userData.getOrganizationMembers().get(0).getName();
            }
            loginEditor.putString("organization", organizationName);

            Log.d(TAG, "Stored user info - Name: " + userData.getFullName() + ", Organization: " + organizationName);
        }

        loginEditor.apply();

        // Set token in API client
        apiClient.setApiToken(response.getToken());

        // Show success message
        updateStatus("Login successful! Redirecting...");
        Toast.makeText(this, "Welcome! Login successful", Toast.LENGTH_SHORT).show();

        // Redirect to main activity
        redirectToMainActivity();
    }

    private void handleLoginError(String errorMessage) {
        String eMessage = "Login failed "+ errorMessage + ". Please try again.";
        updateStatus(eMessage, false);
        Toast.makeText(this, eMessage, Toast.LENGTH_LONG).show();

        // Clear password field
        etPassword.setText("");
        etPassword.requestFocus();
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoadingState(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        etUsername.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (isLoading) {
            btnLogin.setText(R.string.signing_in);
        } else {
            btnLogin.setText(R.string.sign_in);
        }
    }

    private void updateStatus(String message) {
        updateStatus(message, true);
    }
    private void updateStatus(String message, Boolean success) {
        if(!success){
            tvStatus.setTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_error));
        } else {
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.black));
        }
        tvStatus.setText(message);
    }

    // Public method to logout (can be called from other activities)
    public static void logout(android.content.Context context) {
        SharedPreferences authPrefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences loginPrefs = context.getSharedPreferences("login_prefs", MODE_PRIVATE);
        
        authPrefs.edit().clear().apply();
        loginPrefs.edit().clear().apply();

        // Clear token from API client
        ApiClient.getInstance(context).setApiToken("");

        // Redirect to login
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    // Get stored user info
    public static int getUserId(android.content.Context context) {
        SharedPreferences authPrefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return authPrefs.getInt(KEY_USER_ID, -1); // Returns -1 if not found
    }

    public static String getUserName(android.content.Context context) {
        SharedPreferences authPrefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return authPrefs.getString(KEY_USER_NAME, "");
    }

    public static String getUserEmail(android.content.Context context) {
        SharedPreferences authPrefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return authPrefs.getString(KEY_USER_EMAIL, "");
    }

    public static boolean isUserAdmin(android.content.Context context) {
        SharedPreferences authPrefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return authPrefs.getBoolean(KEY_IS_ADMIN, false);
    }

    public static boolean isLoggedIn(android.content.Context context) {
        SharedPreferences authPrefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String token = authPrefs.getString(KEY_TOKEN, "");
        return !TextUtils.isEmpty(token);
    }
}