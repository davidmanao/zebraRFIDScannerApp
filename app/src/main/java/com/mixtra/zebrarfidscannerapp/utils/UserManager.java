package com.mixtra.zebrarfidscannerapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {
    
    private static final String PREFS_NAME = "RfidApp";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ORGANIZATION = "user_organization";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ROLE = "user_role";
    
    private static UserManager instance;
    private SharedPreferences prefs;
    
    private UserManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
        }
        return instance;
    }
    
    // Save user information
    public void saveUserInfo(String name, String email, String organization) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ORGANIZATION, organization);
        editor.apply();
    }
    
    public void saveUserInfo(int userId, String name, String email, String organization, String role) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ORGANIZATION, organization);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }
    
    // Get user information
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User Name");
    }
    
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "user@mixtra.co.id");
    }
    
    public String getUserOrganization() {
        return prefs.getString(KEY_USER_ORGANIZATION, "MIXTRA Corporation");
    }
    
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, 0);
    }
    
    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "Employee");
    }
    
    // Check if user is logged in
    public boolean isUserLoggedIn() {
        return getUserId() > 0 && !getUserName().equals("User Name");
    }
    
    // Clear user data (for logout)
    public void clearUserData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    
    // Update specific fields
    public void updateUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }
    
    public void updateUserEmail(String email) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }
    
    public void updateUserOrganization(String organization) {
        prefs.edit().putString(KEY_USER_ORGANIZATION, organization).apply();
    }
}