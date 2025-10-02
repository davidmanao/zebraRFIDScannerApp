package com.mixtra.zebrarfidscannerapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.mixtra.zebrarfidscannerapp.api.RfidApiManager;
import com.mixtra.zebrarfidscannerapp.databinding.ActivityMainBinding;
import com.mixtra.zebrarfidscannerapp.ui.login.LoginActivity;
import com.mixtra.zebrarfidscannerapp.ui.palletManagement.PalletManagementFragment;
import com.mixtra.zebrarfidscannerapp.ui.PalletActivity.PalletActivityFragment;
import com.mixtra.zebrarfidscannerapp.utils.UserManager;

public class MainActivity extends AppCompatActivity {


    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.bottomAppBar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_pallet_management)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        
        // Set user information in navigation header
        setupNavigationHeader(navigationView);
    }
    
    private void setupNavigationHeader(NavigationView navigationView) {
        // Get the header view
        View headerView = navigationView.getHeaderView(0);
        
        // Find the TextViews
        TextView tvUserName = headerView.findViewById(R.id.tv_user_name);
        TextView tvUserEmail = headerView.findViewById(R.id.tv_user_email);
        TextView tvUserOrganization = headerView.findViewById(R.id.tv_user_organization);
        
        // Get user information using UserManager
        UserManager userManager = UserManager.getInstance(this);
        String userName = userManager.getUserName();
        String userEmail = userManager.getUserEmail();
        String userOrganization = userManager.getUserOrganization();
        
        // Set the user information
        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);
        tvUserOrganization.setText(userOrganization);
    }
    
    // Method to update user information (call this when user data changes)
    public void updateUserInfo(String name, String email, String organization) {
        // Save using UserManager
        UserManager userManager = UserManager.getInstance(this);
        userManager.saveUserInfo(name, email, organization);
        
        // Update the navigation header
        NavigationView navigationView = binding.navView;
        setupNavigationHeader(navigationView);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Logout");
            builder.setMessage("Are you sure you want to logout?");

            builder.setPositiveButton("Logout", (dialog, which) -> {
                performLogout();
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {

        // Clear authentication data
        SharedPreferences authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        SharedPreferences.Editor authEditor = authPrefs.edit();
        authEditor.clear();
        authEditor.apply();

        // Clear user data using UserManager
        UserManager userManager = UserManager.getInstance(this);
        userManager.clearUserData();

        // Clear user data (for backward compatibility)
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor userEditor = userPrefs.edit();
        userEditor.clear();
        userEditor.apply();
        
        // Show logout message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login activity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Delegate key events to fragments if they can handle RFID
        try {
            androidx.fragment.app.Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_content_main);
            
            if (currentFragment != null) {
                // Navigate through child fragments to find the active fragment
                androidx.fragment.app.Fragment navFragment = currentFragment.getChildFragmentManager()
                        .getPrimaryNavigationFragment();
                
                if (navFragment instanceof PalletManagementFragment) {
                    PalletManagementFragment palletFragment = (PalletManagementFragment) navFragment;
                    if (palletFragment.onKeyDown(keyCode, event)) {
                        return true;
                    }
                } else if (navFragment instanceof PalletActivityFragment) {
                    PalletActivityFragment activityFragment = (PalletActivityFragment) navFragment;
                    if (activityFragment.onKeyDown(keyCode, event)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't crash the app
            android.util.Log.e("MainActivity", "Error delegating key event", e);
        }
        
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Delegate key events to PalletManagementFragment if it's the current fragment
        try {
            androidx.fragment.app.Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_content_main);
            
            if (currentFragment != null) {
                // Navigate through child fragments to find PalletManagementFragment
                androidx.fragment.app.Fragment navFragment = currentFragment.getChildFragmentManager()
                        .getPrimaryNavigationFragment();
                
                if (navFragment instanceof PalletManagementFragment) {
                    PalletManagementFragment palletFragment = (PalletManagementFragment) navFragment;
                    if (palletFragment.onKeyUp(keyCode, event)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't crash the app
            android.util.Log.e("MainActivity", "Error delegating key event", e);
        }
        
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}