package com.mixtra.zebrarfidscannerapp.examples;

import com.mixtra.zebrarfidscannerapp.MainActivity;
import com.mixtra.zebrarfidscannerapp.utils.UserManager;

/**
 * Example showing how to set user information for navigation header
 */
public class UserInfoExample {

    /**
     * Example 1: Set user info programmatically in MainActivity
     */
    public static void setUserInfoInMainActivity(MainActivity mainActivity) {
        // Method 1: Using MainActivity's updateUserInfo method
        mainActivity.updateUserInfo(
            "John Smith", 
            "john.smith@mixtra.co.id", 
            "MIXTRA Corporation"
        );
    }
    
    /**
     * Example 2: Set user info directly using UserManager (from any activity/fragment)
     */
    public static void setUserInfoDirectly(android.content.Context context) {
        UserManager userManager = UserManager.getInstance(context);
        
        // Basic user info
        userManager.saveUserInfo(
            "Jane Doe", 
            "jane.doe@mixtra.co.id", 
            "MIXTRA Logistics"
        );
        
        // Or complete user info with ID and role
        userManager.saveUserInfo(
            123,                          // userId
            "Jane Doe",                   // name
            "jane.doe@mixtra.co.id",     // email
            "MIXTRA Logistics",          // organization
            "Manager"                    // role
        );
    }
    
    /**
     * Example 3: Update individual fields
     */
    public static void updateIndividualFields(android.content.Context context) {
        UserManager userManager = UserManager.getInstance(context);
        
        userManager.updateUserName("Updated Name");
        userManager.updateUserEmail("new.email@mixtra.co.id");
        userManager.updateUserOrganization("MIXTRA Warehouse Division");
    }
    
    /**
     * Example 4: Get current user info
     */
    public static void getCurrentUserInfo(android.content.Context context) {
        UserManager userManager = UserManager.getInstance(context);
        
        String name = userManager.getUserName();
        String email = userManager.getUserEmail();
        String organization = userManager.getUserOrganization();
        int userId = userManager.getUserId();
        String role = userManager.getUserRole();
        
        // Use the data as needed
        System.out.println("User: " + name + " (" + email + ")");
        System.out.println("Organization: " + organization);
        System.out.println("Role: " + role);
    }
}