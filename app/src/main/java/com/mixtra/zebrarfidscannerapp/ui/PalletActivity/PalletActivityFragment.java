package com.mixtra.zebrarfidscannerapp.ui.PalletActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.TagData;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PalletActivityFragment extends Fragment implements PalletTransactionAdapter.OnTransactionActionListener, RfidEventsListener {

    private static final String TAG = "PalletActivityFragment";
    
    private FragmentPalletActivityBinding binding;
    private PalletTransactionAdapter adapter;
    private RfidApiService apiService;
    
    // RFID Components
    private Readers readers;
    private RFIDReader reader;
    private boolean isRfidConnected = false;
    private boolean isRfidScanning = false;
    private android.os.Handler rfidTimeoutHandler;
    
    // Pagination and search
    private int currentPage = 0;
    private int itemsPerPage = 50; // Increased to show more items for scrollable demonstration
    private int totalItems = 0;
    private String searchQuery = "";
    private String filterType = ""; // "code" or "rfidtag"
    private Timer searchTimer;
    
    // API Constants

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPalletActivityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RFID timeout handler
        rfidTimeoutHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        
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
                
                // Determine filter type based on search query
                if (!searchQuery.isEmpty()) {
                    // Assume it's a pallet code unless it looks like an RFID tag
                    // RFID tags are usually longer and contain more hex characters
                    if (searchQuery.length() > 10 && searchQuery.matches(".*[A-Fa-f0-9].*")) {
                        filterType = "rfidtag";
                        Log.d(TAG, "Search detected as RFID tag: " + searchQuery);
                    } else {
                        filterType = "code";
                        Log.d(TAG, "Search detected as pallet code: " + searchQuery);
                    }
                } else {
                    filterType = "";
                }
                
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
        
        // Prepare filter parameter
        final String filterParam;
        if (!searchQuery.isEmpty()) {
            if (filterType.equals("rfidtag")) {
                filterParam = "rfidtag:" + searchQuery;
            } else {
                filterParam = "code:" + searchQuery;
            }
        } else {
            filterParam = "";
        }
        
        Log.d(TAG, "Loading transactions with filter: " + filterParam);
        
        // Make API call with server-side filtering
        Call<PalletTransactionListByCodeResponse> call = apiService.getPalletTransactionListByCode(
            itemsPerPage,
            currentPage,
            filterParam
        );

        call.enqueue(new Callback<PalletTransactionListByCodeResponse>() {
            @Override
            public void onResponse(Call<PalletTransactionListByCodeResponse> call, Response<PalletTransactionListByCodeResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    PalletTransactionListByCodeResponse responseBody = response.body();
                    
                    // Debug logs
                    Log.d(TAG, "Total items: " + responseBody.getTotal());
                    Log.d(TAG, "Current page: " + currentPage);
                    Log.d(TAG, "Items per page: " + itemsPerPage);
                    Log.d(TAG, "Data size: " + (responseBody.getData() != null ? responseBody.getData().size() : 0));
                    Log.d(TAG, "Filter applied: " + filterParam);
                    
                    // Update adapter directly with server-filtered data
                    adapter.updateTransactions(responseBody.getData() != null ? responseBody.getData() : new ArrayList<>());
                    
                    // Update pagination info
                    totalItems = responseBody.getTotal();
                    updatePaginationUI();
                    
                    // Update statistics
                    binding.tvTotalTransactions.setText("Total: " + totalItems);
                    if (binding.tvPageInfo != null) {
                        binding.tvPageInfo.setText(String.valueOf(currentPage + 1));
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

    // ===== RFID FUNCTIONALITY =====

    /**
     * Handle hardware key events for RFID scanning
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check for Zebra RFID trigger keys
        if (keyCode == 293 || keyCode == 294 || keyCode == 103 || 
            keyCode == 139 || keyCode == 280 || keyCode == 10036) {
            Log.d(TAG, "RFID trigger pressed - keyCode: " + keyCode);
            
            if (!isRfidScanning) {
                startRfidScan();
            }
            return true;
        }
        return false;
    }

    private void initializeRfidReader() {
        Log.d(TAG, "Initializing RFID reader...");
        
        try {
            // Initialize readers list
            if (readers == null) {
                readers = new Readers(requireContext(), ENUM_TRANSPORT.ALL);
            }
            
            // Get available readers
            if (readers.GetAvailableRFIDReaderList().size() > 0) {
                // Get first available reader (usually the built-in reader)
                ReaderDevice readerDevice = readers.GetAvailableRFIDReaderList().get(0);
                reader = readerDevice.getRFIDReader();
                
                Log.d(TAG, "Found RFID reader: " + readerDevice.getName());
                
                // Connect to the reader
                connectToRfidReader();
                
            } else {
                Log.w(TAG, "No RFID readers found");
                Toast.makeText(getContext(), "No RFID scanner detected", Toast.LENGTH_LONG).show();
            }
            
        } catch (InvalidUsageException e) {
            Log.e(TAG, "Invalid usage exception during RFID initialization", e);
            Toast.makeText(getContext(), "RFID initialization failed - retrying...", Toast.LENGTH_LONG).show();
            
            // Retry after delay
            rfidTimeoutHandler.postDelayed(() -> initializeRfidReader(), 3000);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during RFID initialization", e);
            Toast.makeText(getContext(), "RFID initialization error", Toast.LENGTH_LONG).show();
        }
    }

    private void connectToRfidReader() {
        if (reader == null) {
            Log.w(TAG, "Reader is null, cannot connect");
            return;
        }

        try {
            if (!reader.isConnected()) {
                Log.d(TAG, "Connecting to RFID reader...");
                reader.connect();
                
                if (reader.Events != null) {
                    reader.Events.addEventsListener(this);
                    reader.Events.setHandheldEvent(true);
                    reader.Events.setAttachTagDataWithReadEvent(false);
                    reader.Events.setReaderDisconnectEvent(true);
                    reader.Events.setBatteryEvent(false);
                    reader.Events.setInventoryStartEvent(false);
                    reader.Events.setInventoryStopEvent(false);
                    reader.Events.setTagReadEvent(true);
                }
                
                isRfidConnected = true;
                Log.d(TAG, "RFID reader connected successfully");
                
            } else {
                Log.d(TAG, "RFID reader already connected");
                isRfidConnected = true;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect to RFID reader", e);
            isRfidConnected = false;
            Toast.makeText(getContext(), "Failed to connect to RFID scanner", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRfidScan() {
        // Check if reader is connected
        if (reader == null || !reader.isConnected()) {
            Log.w(TAG, "RFID reader not connected, initializing...");
            Toast.makeText(getContext(), "RFID scanner not ready, initializing...", Toast.LENGTH_SHORT).show();
            initializeRfidReader();
            return;
        }
        
        if (isRfidScanning) {
            Log.d(TAG, "RFID scan already in progress");
            return;
        }
        
        try {
            Log.d(TAG, "Starting RFID scan...");
            isRfidScanning = true;
            reader.Actions.Inventory.perform();
            
            Toast.makeText(getContext(), "Scanning for RFID tags...", Toast.LENGTH_SHORT).show();
            
            // Set timeout
            rfidTimeoutHandler.postDelayed(() -> {
                if (isRfidScanning) {
                    Log.d(TAG, "RFID scan timeout");
                    stopRfidScan();
                    Toast.makeText(getContext(), "No RFID tags found", Toast.LENGTH_SHORT).show();
                }
            }, 3000);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start RFID scan", e);
            isRfidScanning = false;
            Toast.makeText(getContext(), "Failed to start RFID scan", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRfidScan() {
        if (!isRfidScanning || reader == null) {
            return;
        }
        
        try {
            reader.Actions.Inventory.stop();
            isRfidScanning = false;
            Log.d(TAG, "RFID scan stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping RFID scan", e);
            isRfidScanning = false;
        }
        
        // Cancel timeout
        rfidTimeoutHandler.removeCallbacksAndMessages(null);
    }

    // ===== RFID EVENT LISTENERS =====

    @Override
    public void eventReadNotify(RfidReadEvents rfidReadEvents) {
        try {
            if (rfidReadEvents != null && reader != null && reader.Actions != null) {
                TagData[] tagDataArray = reader.Actions.getReadTags(100);
                if (tagDataArray != null) {
                    for (TagData tagData : tagDataArray) {
                        if (tagData != null && tagData.getTagID() != null) {
                            String rfidTag = tagData.getTagID();
                            Log.d(TAG, "RFID tag read: " + rfidTag);
                            
                            // Reset scanning flag
                            isRfidScanning = false;
                            
                            // Stop scanning
                            stopRfidScan();
                            
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    handleRfidTagScanned(rfidTag);
                                });
                            }
                            break; // Process only first tag
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling RFID read event", e);
            isRfidScanning = false;
        }
    }

    @Override
    public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
        Log.d(TAG, "RFID status event: " + rfidStatusEvents.StatusEventData.getStatusEventType());
        
        if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    isRfidConnected = false;
                    isRfidScanning = false;
                    Toast.makeText(getContext(), "RFID scanner disconnected", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void handleRfidTagScanned(String rfidTag) {
        Log.d(TAG, "Processing RFID tag: " + rfidTag);
        
        // Set search query and filter type
        searchQuery = rfidTag;
        filterType = "rfidtag";
        
        // Update search input
        binding.etSearch.setText(rfidTag);
        
        // Reset page and search
        currentPage = 0;
        loadTransactions();
        
        Toast.makeText(getContext(), "RFID tag scanned: " + rfidTag, Toast.LENGTH_SHORT).show();
    }

    private void updatePaginationUI() {
        // Enable/disable pagination buttons
        boolean hasNext = (currentPage + 1) * itemsPerPage < totalItems;
        boolean hasPrev = currentPage > 0;
        
        binding.btnPrevPage.setEnabled(hasPrev);
        binding.btnNextPage.setEnabled(hasNext);
        
        // Make sure buttons are visible
        binding.btnPrevPage.setVisibility(View.VISIBLE);
        binding.btnNextPage.setVisibility(View.VISIBLE);
        
        // Update page info if available
        if (binding.tvPageInfo != null) {
            int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
            binding.tvPageInfo.setText(String.valueOf(currentPage + 1));
        }
        
        // Debug logs
        android.util.Log.d("PalletActivity", "Pagination UI updated:");
        android.util.Log.d("PalletActivity", "Has Previous: " + hasPrev);
        android.util.Log.d("PalletActivity", "Has Next: " + hasNext);
        android.util.Log.d("PalletActivity", "Current Page: " + currentPage);
        android.util.Log.d("PalletActivity", "Total Items: " + totalItems);
        android.util.Log.d("PalletActivity", "Items Per Page: " + itemsPerPage);
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
        
        // Cleanup search timer
        if (searchTimer != null) {
            searchTimer.cancel();
        }
        
        // Cleanup RFID resources
        if (rfidTimeoutHandler != null) {
            rfidTimeoutHandler.removeCallbacksAndMessages(null);
        }
        
        try {
            if (reader != null) {
                if (isRfidScanning) {
                    reader.Actions.Inventory.stop();
                }
                if (reader.Events != null) {
                    reader.Events.removeEventsListener(this);
                }
                if (reader.isConnected()) {
                    reader.disconnect();
                }
            }
            if (readers != null) {
                readers.Dispose();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error during RFID cleanup: " + e.getMessage());
        }
        
        binding = null;
    }
}