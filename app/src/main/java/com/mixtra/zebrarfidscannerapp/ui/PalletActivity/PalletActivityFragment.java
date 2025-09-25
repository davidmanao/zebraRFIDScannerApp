package com.mixtra.zebrarfidscannerapp.ui.PalletActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.api.ApiClient;
import com.mixtra.zebrarfidscannerapp.api.RfidApiService;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionListByCodeResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionListResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionResponse;
import com.mixtra.zebrarfidscannerapp.databinding.FragmentPalletActivityBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PalletActivityFragment extends Fragment implements PalletTransactionAdapter.OnTransactionActionListener {

    private FragmentPalletActivityBinding binding;
    private PalletTransactionAdapter adapter;
    private RfidApiService apiService;
    
    // Pagination and search
    private int currentPage = 0;
    private int itemsPerPage = 20; // Use a more reasonable page size
    private int totalItems = 0;
    private String searchQuery = "";
    private Timer searchTimer;
    
    // API Constants

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPalletActivityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initializeViews();
        setupClickListeners();
        loadTransactions();

        return root;
    }

    private void initializeViews() {
        // Initialize API service with token from ApiClient
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        apiService = apiClient.getApiService();
        
        // Setup RecyclerView
        adapter = new PalletTransactionAdapter(this);
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTransactions.setAdapter(adapter);
        
        // Initialize pagination UI with default values
        totalItems = 100; // Temporary test value
        updatePaginationUI();
        
        // Update statistics with initial values
        binding.tvTotalTransactions.setText("Total: " + totalItems);
        binding.tvCurrentPage.setText("Page: " + (currentPage + 1));
        
        // Log initialization
        android.util.Log.d("PalletActivity", "Views initialized. Pagination buttons should be visible.");
    }

    private void setupClickListeners() {
        // Search functionality with debounce
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                
                // Cancel previous timer
                if (searchTimer != null) {
                    searchTimer.cancel();
                }
                
                // Start new timer for debounced search
                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(() -> {
                            currentPage = 0;
                            loadTransactions();
                        });
                    }
                }, 500); // 500ms debounce
            }
        });

        // Pagination buttons
        binding.btnPrevPage.setOnClickListener(v -> {
            android.util.Log.d("PalletActivity", "Previous button clicked. Current page: " + currentPage);
            if (currentPage > 0) {
                currentPage--;
                android.util.Log.d("PalletActivity", "Moving to page: " + currentPage);
                loadTransactions();
            }
        });

        binding.btnNextPage.setOnClickListener(v -> {
            android.util.Log.d("PalletActivity", "Next button clicked. Current page: " + currentPage + ", Total items: " + totalItems);
            if ((currentPage + 1) * itemsPerPage < totalItems) {
                currentPage++;
                android.util.Log.d("PalletActivity", "Moving to page: " + currentPage);
                loadTransactions();
            }
        });
    }

    private void loadTransactions() {
        // Show loading
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Make API call
        Call<PalletTransactionListByCodeResponse> call = apiService.getPalletTransactionListByCode(
            itemsPerPage,
            currentPage
        );

        call.enqueue(new Callback<PalletTransactionListByCodeResponse>() {
            @Override
            public void onResponse(Call<PalletTransactionListByCodeResponse> call, Response<PalletTransactionListByCodeResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    PalletTransactionListByCodeResponse responseBody = response.body();
                    
                    // Debug logs
                    android.util.Log.d("PalletActivity", "Total items: " + responseBody.getTotal());
                    android.util.Log.d("PalletActivity", "Current page: " + currentPage);
                    android.util.Log.d("PalletActivity", "Items per page: " + itemsPerPage);
                    android.util.Log.d("PalletActivity", "Data size: " + (responseBody.getData() != null ? responseBody.getData().size() : 0));
                    
                    // Filter data based on search query
                    List<PalletTransactionListByCodeResponse.PalletTransactionItem> filteredData =
                        filterTransactions(responseBody.getData(), searchQuery);
                    
                    // Update adapter
                    adapter.updateTransactions(filteredData);
                    
                    // Update pagination info
                    totalItems = responseBody.getTotal();
                    updatePaginationUI();
                    
                    // Update statistics
                    binding.tvTotalTransactions.setText("Total: " + totalItems);
                    binding.tvCurrentPage.setText("Page: " + (currentPage + 1));
                    if (binding.tvPageInfo != null) {
                        binding.tvPageInfo.setText("Page " + (currentPage + 1) + " of " + 
                            Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage)));
                    }
                    
                } else {
                    showErrorMessage("Failed to load transactions: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PalletTransactionListByCodeResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                showErrorMessage("Network error: " + t.getMessage());
            }
        });
    }

    private List<PalletTransactionListByCodeResponse.PalletTransactionItem> filterTransactions(
            List<PalletTransactionListByCodeResponse.PalletTransactionItem> transactions, String query) {
        
        if (query.isEmpty()) {
            return transactions;
        }
        
        List<PalletTransactionListByCodeResponse.PalletTransactionItem> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (PalletTransactionListByCodeResponse.PalletTransactionItem transaction : transactions) {
            String palletCode = transaction.getPalletCode();
            if (String.valueOf(transaction.getId()).contains(lowerQuery) ||
                transaction.getType().toLowerCase().contains(lowerQuery) ||
                (palletCode != null && palletCode.toLowerCase().contains(lowerQuery))) {
                filtered.add(transaction);
            }
        }
        
        return filtered;
    }

    private void updatePaginationUI() {
        // Enable/disable pagination buttons
        boolean hasNext = (currentPage + 1) * itemsPerPage < totalItems;
        boolean hasPrev = currentPage > 0;
        
        binding.btnPrevPage.setEnabled(hasPrev);
        binding.btnNextPage.setEnabled(hasNext);
        
        // Make sure buttons and container are visible
        binding.btnPrevPage.setVisibility(View.VISIBLE);
        binding.btnNextPage.setVisibility(View.VISIBLE);
        
        // Make sure the pagination container is visible
        if (binding.llPaginationControls != null) {
            binding.llPaginationControls.setVisibility(View.VISIBLE);
        }
        
        // Update page info if available
        if (binding.tvPageInfo != null) {
            int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
            binding.tvPageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
        }
        
        // Debug logs
        android.util.Log.d("PalletActivity", "Pagination UI updated:");
        android.util.Log.d("PalletActivity", "Has Previous: " + hasPrev);
        android.util.Log.d("PalletActivity", "Has Next: " + hasNext);
        android.util.Log.d("PalletActivity", "Current Page: " + currentPage);
        android.util.Log.d("PalletActivity", "Total Items: " + totalItems);
        android.util.Log.d("PalletActivity", "Items Per Page: " + itemsPerPage);
        android.util.Log.d("PalletActivity", "Pagination container visible: " + 
            (binding.llPaginationControls != null ? binding.llPaginationControls.getVisibility() == View.VISIBLE : "null"));
    }

    @Override
    public void onTransactionClicked(PalletTransactionListByCodeResponse.PalletTransactionItem transaction) {
        // Show loading
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Call API to get full transaction data
        Call<PalletTransactionResponse> call = apiService.getPalletTransaction(transaction.getId());
        
        call.enqueue(new Callback<PalletTransactionResponse>() {
            @Override
            public void onResponse(Call<PalletTransactionResponse> call, Response<PalletTransactionResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    // Navigate to transaction form with full transaction data
                    navigateToTransactionForm(response.body(), transaction.getId());
                } else {
                    showErrorMessage("Failed to load transaction details: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PalletTransactionResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                showErrorMessage("Network error: " + t.getMessage());
            }
        });
    }
    
    private void navigateToTransactionForm(PalletTransactionResponse transactionData, int transactionId) {
        // Create bundle to pass transaction data to the form
        Bundle bundle = new Bundle();
        bundle.putInt("transactionId", transactionId);
        
        // Add transaction details from API response
        if (transactionData.getData() != null) {
            PalletTransactionResponse.TransactionData data = transactionData.getData();
            bundle.putInt("palletId", data.getPalletId());
            bundle.putString("transactionType", data.getTransactionType());
            bundle.putString("type", data.getType());
            bundle.putString("transactionDate", data.getTransactionDate());
            bundle.putString("status", data.getStatus());
            
            // Add pallet code if available
            if (data.getPallet() != null && data.getPallet().getCode() != null) {
                bundle.putString("palletCode", data.getPallet().getCode());
            }
        }
        
        try {
            Navigation.findNavController(requireView()).navigate(R.id.nav_transaction_form, bundle);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Navigation not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTransaction(int transactionId) {
        // Show loading
        binding.progressBar.setVisibility(View.VISIBLE);
        
        Call<Void> call = apiService.deletePalletTransaction( transactionId);
        
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
                    // Reload transactions
                    loadTransactions();
                } else {
                    showErrorMessage("Failed to delete transaction: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                showErrorMessage("Network error: " + t.getMessage());
            }
        });
    }

    private void showErrorMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchTimer != null) {
            searchTimer.cancel();
        }
        binding = null;
    }
}