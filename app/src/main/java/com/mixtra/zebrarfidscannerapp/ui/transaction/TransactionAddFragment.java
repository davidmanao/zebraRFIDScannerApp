package com.mixtra.zebrarfidscannerapp.ui.transaction;

import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;

import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.api.ApiClient;
import com.mixtra.zebrarfidscannerapp.api.RfidApiService;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionRequest;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionResponse;
import com.mixtra.zebrarfidscannerapp.databinding.FragmentTransactionDetailsBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TransactionAddFragment extends Fragment {

    private FragmentTransactionDetailsBinding binding;
    private boolean isInTransaction = true; // true for IN, false for OUT
    private Calendar selectedDate;
    private RfidApiService apiService;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize API service
        apiService = ApiClient.getInstance(requireContext()).getApiService();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTransactionDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initializeViews();
        setupClickListeners();

        return root;
    }

    private void initializeViews() {
        
        // Initialize date with current date
        selectedDate = Calendar.getInstance();
        updateDateDisplay();
        
        // Set initial transaction type
        updateTransactionType(true);
    }

    private void setupClickListeners() {
        // Date picker
        binding.etTransactionDate.setOnClickListener(v -> showDatePicker());

        // Toggle switch
        View toggleArea = binding.getRoot().findViewById(R.id.toggle_background);
        toggleArea.setOnClickListener(v -> toggleTransactionType());

        // Cancel button
        binding.btnCancel.setOnClickListener(v -> {
            // Navigate back
            Navigation.findNavController(v).popBackStack();
        });

        // Save items button
        binding.btnSaveItems.setOnClickListener(v -> {
            String voucherNumber = binding.etVoucherNumber.getText().toString().trim();
            
            if (validateInputs(voucherNumber)) {
                // Show loading state
                binding.btnSaveItems.setEnabled(false);
                binding.btnSaveItems.setText("Saving...");
                
                // Call API
                createPalletTransaction(voucherNumber);
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                updateDateDisplay();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        binding.etTransactionDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void toggleTransactionType() {
        isInTransaction = !isInTransaction;
        updateTransactionType(true);
    }

    private void updateTransactionType(boolean animate) {
        View toggleButton = binding.getRoot().findViewById(R.id.toggle_button);
        
        if (animate) {
            // Animate toggle button
            float targetX = isInTransaction ? 0f : 42f; // 80dp - 36dp - 2dp = 42dp
            ObjectAnimator animator = ObjectAnimator.ofFloat(toggleButton, "translationX", targetX);
            animator.setDuration(200);
            animator.start();
        } else {
            toggleButton.setTranslationX(isInTransaction ? 0f : 42f);
        }

        // Update text colors
        if (isInTransaction) {
            binding.tvIn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            binding.tvOut.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        } else {
            binding.tvIn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
            binding.tvOut.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        }
    }

    private boolean validateInputs(String voucherNumber) {
        if (voucherNumber.isEmpty()) {
            binding.etVoucherNumber.setError("Voucher number is required");
            binding.etVoucherNumber.requestFocus();
            return false;
        }
        return true;
    }

    private void createPalletTransaction(String voucherNumber) {
        // Prepare request
        String transactionType = isInTransaction ? "Receiving" : "Shipping";
        String type = isInTransaction ? "IN" : "OUT";
        
        // Format date to ISO 8601
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = sdf.format(selectedDate.getTime());
        
        PalletTransactionRequest request = new PalletTransactionRequest(
            1, // palletId - you might want to get this from previous form
            formattedDate,
            transactionType,
            type
        );

        // Make API call
        Call<PalletTransactionResponse> call = apiService.createPalletTransaction(
            request
        );

        call.enqueue(new Callback<PalletTransactionResponse>() {
            @Override
            public void onResponse(Call<PalletTransactionResponse> call, Response<PalletTransactionResponse> response) {
                // Reset button state
                binding.btnSaveItems.setEnabled(true);
                binding.btnSaveItems.setText("Save items");

                if (response.isSuccessful() && response.body() != null) {
                    // Success - get the transaction details
                    int transactionId = response.body().getId();
                    getPalletTransaction(transactionId);
                } else {
                    // Error
                    showErrorMessage("Failed to create transaction: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PalletTransactionResponse> call, Throwable t) {
                // Reset button state
                binding.btnSaveItems.setEnabled(true);
                binding.btnSaveItems.setText("Save items");
                
                // Network error
                showErrorMessage("Network error: " + t.getMessage());
            }
        });
    }

    private void getPalletTransaction(int transactionId) {
        Call<PalletTransactionResponse> call = apiService.getPalletTransaction(transactionId);

        call.enqueue(new Callback<PalletTransactionResponse>() {
            @Override
            public void onResponse(Call<PalletTransactionResponse> call, Response<PalletTransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Success - show transaction form
                    showTransactionForm(response.body());
                } else {
                    showErrorMessage("Failed to get transaction details: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PalletTransactionResponse> call, Throwable t) {
                showErrorMessage("Network error: " + t.getMessage());
            }
        });
    }

    private void showTransactionForm(PalletTransactionResponse transaction) {
        // Navigate to transaction form fragment with data
        try {
            // Create bundle to pass transaction data
            Bundle bundle = new Bundle();
            // We could serialize the transaction data, but for simplicity, 
            // let's just pass basic info and let the form handle it
            if (transaction.getData() != null) {
                bundle.putInt("transactionId", transaction.getData().getId());
                bundle.putInt("palletId", transaction.getData().getPalletId());
                bundle.putString("transactionType", transaction.getData().getType());
                bundle.putString("transactionDate", transaction.getData().getTransactionDate());
                
                if (transaction.getData().getPallet() != null) {
                    bundle.putString("palletCode", transaction.getData().getPallet().getCode());
                    bundle.putString("rfidTag", transaction.getData().getPallet().getRfidTag());
                }
            }
            
            Navigation.findNavController(requireView()).navigate(R.id.nav_transaction_form, bundle);
        } catch (Exception e) {
            // Fallback - show success message
            Toast.makeText(getContext(), 
                "Transaction created successfully! ID: " + transaction.getId(), 
                Toast.LENGTH_LONG).show();
            
            // Navigate back to home
            Navigation.findNavController(requireView()).popBackStack(R.id.nav_home, false);
        }
    }

    private void showErrorMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}