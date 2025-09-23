package com.mixtra.zebrarfidscannerapp.ui.palletManagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.api.ApiClient;
import com.mixtra.zebrarfidscannerapp.api.RfidApiService;
import com.mixtra.zebrarfidscannerapp.api.model.PalletListResponse;
import com.mixtra.zebrarfidscannerapp.databinding.FragmentPalletManagementBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PalletManagementFragment extends Fragment {

    private static final String TAG = "PalletManagementFragment";
    private static final int ITEMS_PER_PAGE = 10;

    private FragmentPalletManagementBinding binding;
    private PalletAdapter palletAdapter;
    private RfidApiService apiService;
    
    // Activity Result Launcher for Edit Pallet
    private ActivityResultLauncher<Intent> editPalletLauncher;
    
    // Pagination
    private int currentPage = 0;
    private int totalItems = 0;
    private String currentSearchQuery = "";

    // UI Components
    private RecyclerView recyclerViewPallets;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvError;
    private TextView tvTotalCount;
    private TextView tvPageInfo;
    private EditText etSearch;
    private Button btnSearch;
    private Button btnPrevious;
    private Button btnNext;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        binding = FragmentPalletManagementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize API service
        apiService = ApiClient.getInstance(requireContext()).getApiService();

        // Initialize Activity Result Launcher
        initializeEditPalletLauncher();

        // Initialize UI components
        initializeViews();
        setupRecyclerView();
        setupSwipeRefresh();
        setupEventListeners();

        // Load initial data
        loadPalletData(currentPage, "");

        return root;
    }

    private void initializeEditPalletLauncher() {
        editPalletLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    String updatedRfidTag = result.getData().getStringExtra(EditPalletActivity.RESULT_EXTRA_UPDATED_RFID_TAG);
                    if (updatedRfidTag != null) {
                        // Refresh the current page to show updated data
                        loadPalletData(currentPage, currentSearchQuery);
                        Toast.makeText(getContext(), "Pallet updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void initializeViews() {
        recyclerViewPallets = binding.recyclerViewPallets;
        swipeRefreshLayout = binding.swipeRefreshLayout;
        progressBar = binding.progressBar;
        tvError = binding.tvError;
        tvTotalCount = binding.tvTotalCount;
        tvPageInfo = binding.tvPageInfo;
        etSearch = binding.etSearch;
        btnSearch = binding.btnSearch;
        btnPrevious = binding.btnPrevious;
        btnNext = binding.btnNext;
    }

    private void setupRecyclerView() {
        palletAdapter = new PalletAdapter();
        recyclerViewPallets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPallets.setAdapter(palletAdapter);

        // Set click listener for pallet items
        palletAdapter.setOnPalletClickListener(pallet -> {
            // Open edit dialog
            openEditPalletDialog(pallet);
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reset to first page and reload data
            currentPage = 0;
            loadPalletData(currentPage, currentSearchQuery);
        });
        
        // Customize refresh indicator colors
        swipeRefreshLayout.setColorSchemeResources(
            R.color.purple_500,
            R.color.purple_700
        );
    }

    private void setupEventListeners() {
        // Search button click
        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            currentSearchQuery = query;
            currentPage = 0; // Reset to first page
            loadPalletData(currentPage, query);
        });

        // Search on enter key
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            btnSearch.performClick();
            return true;
        });

        // Pagination buttons
        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadPalletData(currentPage, currentSearchQuery);
            }
        });

        btnNext.setOnClickListener(v -> {
            int maxPage = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE) - 1;
            if (currentPage < maxPage) {
                currentPage++;
                loadPalletData(currentPage, currentSearchQuery);
            }
        });
    }

    private void loadPalletData(int page, String searchQuery) {
        showLoading(true);
        hideError();

        Call<PalletListResponse> call;
        
        if (TextUtils.isEmpty(searchQuery)) {
            // Load all pallets
            call = apiService.getPalletList(ITEMS_PER_PAGE, page);
        } else {
            // Search with filter
            String filter = "code:" + searchQuery; // You can modify this filter format as needed
            call = apiService.getPalletList(filter, ITEMS_PER_PAGE, page);
        }

        call.enqueue(new Callback<PalletListResponse>() {
            @Override
            public void onResponse(@NonNull Call<PalletListResponse> call, 
                                 @NonNull Response<PalletListResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    PalletListResponse palletResponse = response.body();
                    
                    // Update data
                    palletAdapter.setPalletList(palletResponse.getData());
                    totalItems = palletResponse.getTotal();
                    
                    // Update UI
                    updateTotalCount();
                    updatePaginationInfo();
                    updatePaginationButtons();
                    
                    Log.d(TAG, "Loaded " + (palletResponse.getData() != null ? 
                           palletResponse.getData().size() : 0) + " pallets");
                } else {
                    showError("Failed to load pallet data: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PalletListResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to load pallet data", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        // Stop swipe refresh if it's active
        if (!show && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        
        // Only show progress bar if not refreshing via swipe
        if (show && !swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewPallets.setVisibility(View.GONE);
        } else if (!show) {
            progressBar.setVisibility(View.GONE);
            recyclerViewPallets.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        recyclerViewPallets.setVisibility(View.GONE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
        recyclerViewPallets.setVisibility(View.VISIBLE);
    }

    private void updateTotalCount() {
        String countText = "Total Pallets: " + totalItems;
        if (!TextUtils.isEmpty(currentSearchQuery)) {
            countText += " (filtered)";
        }
        tvTotalCount.setText(countText);
    }

    private void updatePaginationInfo() {
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        totalPages = Math.max(1, totalPages); // At least 1 page
        String pageInfo = "Page " + (currentPage + 1) + " of " + totalPages;
        tvPageInfo.setText(pageInfo);
    }

    private void updatePaginationButtons() {
        btnPrevious.setEnabled(currentPage > 0);
        
        int maxPage = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE) - 1;
        btnNext.setEnabled(currentPage < maxPage && totalItems > 0);
    }

    private void openEditPalletDialog(PalletListResponse.PalletData pallet) {
        Intent editIntent = EditPalletActivity.createIntent(
            requireContext(),
            pallet.getId(),
            pallet.getCode(),
            pallet.getName(),
            pallet.getType(),
            pallet.getCapacity(),
            pallet.getTare(),
            pallet.getInitial(),
            pallet.getIncoming(),
            pallet.getOutgoing(),
            pallet.getBalance(),
            pallet.getRfidTag()
        );
        
        editPalletLauncher.launch(editIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}