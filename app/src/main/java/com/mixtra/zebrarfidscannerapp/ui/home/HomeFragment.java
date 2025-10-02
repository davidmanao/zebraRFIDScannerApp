package com.mixtra.zebrarfidscannerapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.databinding.FragmentHomeBinding;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get username from SharedPreferences
        android.content.SharedPreferences prefs = requireActivity()
                .getSharedPreferences("login_prefs", android.content.Context.MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");
        String organization = prefs.getString("organization", "Unknown Organization");

        // Set username and organization to TextViews
        binding.tvUserName.setText(username);
        binding.tvOrganization.setText(organization);
        binding.greetingText.setText(getGreeting());
        
        // Set click listeners for cards
        binding.cardScanPallet.setOnClickListener(v -> {
            // Navigate to RFID input form  
            try {
                Navigation.findNavController(v).navigate(R.id.action_nav_home_to_nav_rfid_input);
            } catch (Exception e) {
                // Fallback - for now just show a toast
                android.widget.Toast.makeText(getContext(), "Navigation to RFID form", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.cardTransactions.setOnClickListener(v -> {
            // Navigate to pallet activity list
            try {
                Navigation.findNavController(v).navigate(R.id.action_nav_home_to_nav_pallet_activity);
            } catch (Exception e) {
                // Fallback - show error message
                android.widget.Toast.makeText(getContext(), "Navigation to pallet activity failed", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        
        // Temporarily commented out - IN/OUT cards are hidden until ready
        /*
        binding.cardIn.setOnClickListener(v -> {
            // Navigate to IN transactions
            // TODO: Implement navigation
        });
        
        binding.cardOut.setOnClickListener(v -> {
            // Navigate to OUT transactions
            // TODO: Implement navigation
        });
        */
        
        return root;
    }


    private String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Good morning";
        } else if (hour >= 12 && hour < 18) {
            return "Good afternoon";
        } else if (hour >= 18 && hour < 22) {
            return "Good evening";
        } else {
            return "Good night";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}