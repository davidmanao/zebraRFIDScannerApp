package com.mixtra.zebrarfidscannerapp.ui.rfid;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.api.ApiClient;
import com.mixtra.zebrarfidscannerapp.api.RfidApiService;
import com.mixtra.zebrarfidscannerapp.api.model.PalletCodeRequest;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionResponse;
import com.mixtra.zebrarfidscannerapp.databinding.FragmentRfidInputBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RfidInputFragment extends Fragment {

    private FragmentRfidInputBinding binding;
    private RfidApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRfidInputBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize API service
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        apiService = apiClient.getApiService();

        setupClickListeners();

        return root;
    }

    private void setupClickListeners() {
        // Camera/QR scan area click listener
        binding.cameraSection.setOnClickListener(v -> {
            // TODO: Implement camera/QR code scanning functionality
            Toast.makeText(getContext(), "Camera scanning not implemented yet", Toast.LENGTH_SHORT).show();
        });

        // Add Pallet button click listener
        binding.btnAddPallet.setOnClickListener(v -> {
            String codePallet = binding.etCodePallet.getText().toString().trim();

            if (validateInputs(codePallet)) {
                createPalletTransaction(codePallet);
            }
        });
    }

    private boolean validateInputs(String code) {
        if (code.isEmpty()) {
            binding.etCodePallet.setError("Code is required");
            binding.etCodePallet.requestFocus();
            return false;
        }


        return true;
    }

    private void createPalletTransaction(String palletCode) {
        // Show loading state
        binding.btnAddPallet.setEnabled(false);
        binding.btnAddPallet.setText("Creating...");
        
        // Create request with pallet code
        PalletCodeRequest request = new PalletCodeRequest(palletCode);
        
        // Make API call to create pallet transaction
        Call<PalletTransactionResponse> call = apiService.createPalletTransactionWithCode(
            request
        );
        
        call.enqueue(new Callback<PalletTransactionResponse>() {
            @Override
            public void onResponse(Call<PalletTransactionResponse> call, Response<PalletTransactionResponse> response) {
                // Reset button state
                binding.btnAddPallet.setEnabled(true);
                binding.btnAddPallet.setText("Add Pallet");
                
                if (response.isSuccessful() && response.body() != null) {
                    PalletTransactionResponse transactionResponse = response.body();
                    
                    Log.d("RfidInput", "Create transaction response received");
                    Log.d("RfidInput", "Response body: " + (transactionResponse != null ? "not null" : "null"));
                    Log.d("RfidInput", "Response data: " + (transactionResponse != null && transactionResponse.getData() != null ? "not null" : "null"));
                    
                    // Check if response data is available
                    if (transactionResponse.getData() != null) {
                        // Extract transaction ID from response
                        int transactionId = transactionResponse.getData().getId();
                        
                        Log.d("RfidInput", "Transaction created successfully with ID: " + transactionId);
                        
                        // Clear the input field
                        clearFields();
                        
                        // Navigate to transaction form with the returned transaction ID
                        navigateToTransactionForm(transactionId, palletCode);
                    } else {
                        // Handle case where data is null but response is successful
                        Log.w("RfidInput", "Transaction created but no data returned in response");
                        Log.w("RfidInput", "Result message: " + (transactionResponse.getResultMsg() != null ? transactionResponse.getResultMsg() : "null"));
                        showErrorMessage("Transaction created but no details returned. Please check transaction list.");
                        clearFields();
                    }
                    
                } else {
                    Log.e("RfidInput", "Create transaction failed - Response code: " + response.code());
                    Log.e("RfidInput", "Error message: " + response.message());
                    showErrorMessage("Failed to create pallet transaction: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PalletTransactionResponse> call, Throwable t) {
                // Reset button state
                binding.btnAddPallet.setEnabled(true);
                binding.btnAddPallet.setText("Add Pallet");
                
                showErrorMessage("Network error: " + t.getMessage());
            }
        });
    }
    
    private void navigateToTransactionForm(int transactionId, String palletCode) {
        // Create bundle with transaction data
        Bundle bundle = new Bundle();
        bundle.putInt("transactionId", transactionId);
        bundle.putString("palletCode", palletCode);
        
        try {
            Navigation.findNavController(requireView()).navigate(R.id.nav_transaction_form, bundle);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Navigation to transaction form failed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showErrorMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void clearFields() {
        binding.etCodePallet.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}