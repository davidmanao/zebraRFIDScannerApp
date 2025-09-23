package com.mixtra.zebrarfidscannerapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mixtra.zebrarfidscannerapp.databinding.FragmentHomeBinding;

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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}