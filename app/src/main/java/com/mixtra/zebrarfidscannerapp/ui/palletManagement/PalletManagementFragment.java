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
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.TagData;

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
    // private SwipeRefreshLayout swipeRefreshLayout; // Removed - not in new layout
    private ProgressBar progressBar;
    // private TextView tvError; // Removed - not in new layout  
    private TextView tvTotalCount;
    private TextView tvPageInfo;
    private EditText etSearch;
    // private Button btnSearch; // Removed - not in new layout
    private Button btnPrevious;
    private Button btnNext;

    // RFID Components
    private Readers readers;
    private RFIDReader reader;
    private boolean isRfidConnected = false;
    private boolean isRfidScanning = false;
    private android.os.Handler rfidTimeoutHandler = new android.os.Handler(android.os.Looper.getMainLooper());

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

        // Initialize RFID
        initializeRfid();

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
        // swipeRefreshLayout = binding.swipeRefreshLayout; // Removed - not in new layout
        progressBar = binding.progressBar;
        // tvError = binding.tvError; // Removed - not in new layout
        tvTotalCount = binding.tvTotalCount;
        tvPageInfo = binding.tvPageInfo;
        etSearch = binding.etSearch;
        // btnSearch = binding.btnSearch; // Removed - not in new layout
        btnPrevious = binding.btnPrevPage; // Updated ID
        btnNext = binding.btnNextPage; // Updated ID
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
        // Swipe refresh removed for simplified data table view
    }

    private void setupEventListeners() {
        // Search functionality integrated into text change
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String query = s.toString().trim();
                currentSearchQuery = "Search : " + query;
                currentPage = 0; // Reset to first page
                loadPalletData(currentPage, query);
            }
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
            String filter = searchQuery; // You can modify this filter format as needed
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
        // Simplified loading for data table view
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewPallets.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerViewPallets.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        // Show error via Toast for simplified data table view
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        recyclerViewPallets.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        // No-op for simplified data table view
        recyclerViewPallets.setVisibility(View.VISIBLE);
    }

    private void updateTotalCount() {
        String countText = "Total Pallets: " + totalItems;
        if (!TextUtils.isEmpty(currentSearchQuery)) {
            countText += " (filtered)";
        }
        tvTotalCount.setText(countText);
    }

    // ========================
    // RFID INITIALIZATION
    // ========================

    private void initializeRfid() {
        Log.d("RFID", "=== INITIALIZING RFID ===");
        new Thread(() -> {
            try {
                Log.d("RFID", "Creating Readers object...");
                readers = new Readers(requireContext(), ENUM_TRANSPORT.ALL);
                Log.d("RFID", "Readers object created: " + (readers != null ? "success" : "failed"));
                
                requireActivity().runOnUiThread(() -> {
                    Log.d("RFID", "RFID SDK initialized successfully");
                    connectToRfidReader();
                });
            } catch (Exception e) {
                Log.e("RFID", "Failed to initialize RFID SDK: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "RFID initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void connectToRfidReader() {
        Log.d("RFID", "=== CONNECTING TO RFID READER ===");
        new Thread(() -> {
            try {
                if (readers != null) {
                    Log.d("RFID", "Getting available RFID reader list...");
                    java.util.ArrayList<ReaderDevice> readerDevices = readers.GetAvailableRFIDReaderList();
                    Log.d("RFID", "Found " + readerDevices.size() + " RFID readers");
                    
                    if (readerDevices.size() > 0) {
                        ReaderDevice readerDevice = readerDevices.get(0);
                        Log.d("RFID", "Connecting to reader: " + readerDevice.getName());
                        reader = readerDevice.getRFIDReader();
                        
                        if (reader != null) {
                            Log.d("RFID", "Reader object obtained, attempting connection...");
                            reader.connect();
                            Log.d("RFID", "Reader connection successful!");
                            
                            // Configure RFID reader settings
                            configureRfidReader();
                            
                            requireActivity().runOnUiThread(() -> {
                                isRfidConnected = true;
                                setupRfidEventListeners();
                                Log.d("RFID", "✓ Connected to RFID reader: " + readerDevice.getName());
                                Toast.makeText(getContext(), "RFID Reader Connected - Press scan trigger to read tags", Toast.LENGTH_LONG).show();
                            });
                        } else {
                            Log.e("RFID", "✗ Failed to get reader object from device");
                        }
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            Log.w("RFID", "✗ No RFID readers found");
                            Toast.makeText(getContext(), "No RFID readers found", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("RFID", "Failed to connect to RFID reader: " + e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    isRfidConnected = false;
                    Toast.makeText(getContext(), "RFID connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void configureRfidReader() {
        if (reader != null) {
            try {
                // Basic configuration to ensure reader is ready
                Log.d("RFID", "RFID reader basic configuration completed");
                
                // Note: Advanced configuration can be added here if needed
                // For now, using default settings which should work for most scenarios
                
            } catch (Exception e) {
                Log.w("RFID", "Could not configure RFID reader settings: " + e.getMessage());
                // Continue anyway - basic functionality should still work
            }
        }
    }
    private void setupRfidEventListeners() {
        if (reader != null) {
            try {
                Log.d("RFID", "=== SETTING UP RFID EVENT LISTENERS ===");
                
                // Configure RFID events (crucial for tag detection!)
                if (reader.Events != null) {
                    reader.Events.addEventsListener(new RfidEventsListener() {
                    @Override
                    public void eventReadNotify(RfidReadEvents rfidReadEvents) {
                        try {
                            if (rfidReadEvents != null && reader != null && reader.Actions != null) {
                                TagData[] tagDataArray = reader.Actions.getReadTags(100);
                                if (tagDataArray != null && tagDataArray.length > 0) {
                                    for (TagData tagData : tagDataArray) {
                                        if (tagData != null && tagData.getTagID() != null) {
                                            String rfidTag = tagData.getTagID();
                                            Log.d("RFID", "RFID tag detected: " + rfidTag);
                                            
                                            // Reset scanning flag
                                            isRfidScanning = false;
                                            
                                            // Stop scanning
                                            stopRfidScanning();
                                            
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> {
                                                    handleRfidTagRead(rfidTag);
                                                });
                                            }
                                            break; // Process only first tag
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("RFID", "Error processing RFID read event: " + e.getMessage());
                        }
                    }

                    @Override
                    public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.d("RFID", "RFID Status event: " + rfidStatusEvents.StatusEventData.getStatusEventType());
                            });
                        }
                    }
                });
                
                // ✅ CRITICAL: Configure RFID events (this was missing!)
                Log.d("RFID", "Configuring RFID event settings...");
                reader.Events.setHandheldEvent(true);
                reader.Events.setAttachTagDataWithReadEvent(false);
                reader.Events.setReaderDisconnectEvent(true);
                reader.Events.setBatteryEvent(false);
                reader.Events.setInventoryStartEvent(false);
                reader.Events.setInventoryStopEvent(false);
                reader.Events.setTagReadEvent(true); // ← This is crucial for tag detection!
                Log.d("RFID", "✓ RFID event configuration completed");
                
                } else {
                    Log.e("RFID", "✗ reader.Events is null - cannot configure");
                }
            } catch (InvalidUsageException | OperationFailureException e) {
                Log.e("RFID", "Error setting up event listeners: " + e.getMessage());
            }
        }
    }

    private void handleRfidTagRead(String tagId) {
        Log.d("RFID", "RFID tag read: " + tagId);
        
        // Direct pallet lookup using getPalletByRfid endpoint
        if (tagId != null && !tagId.trim().isEmpty()) {
            String cleanTag = tagId.trim().toUpperCase();
            
            // Show immediate feedback
            Toast.makeText(getContext(), "RFID Tag scanned: " + cleanTag, Toast.LENGTH_SHORT).show();
            
            // Optional: Add vibration feedback
            try {
                android.os.Vibrator vibrator = (android.os.Vibrator) requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(200); // 200ms vibration
                }
            } catch (Exception e) {
                Log.w("RFID", "Vibration not available: " + e.getMessage());
            }
            
            // Direct API call to get pallet by RFID
            getPalletByRfidAndOpen(cleanTag);
        }
    }

    private void getPalletByRfidAndOpen(String rfidTag) {
        Log.d("RFID", "Getting pallet by RFID: " + rfidTag);
        showLoading(true);

        // Direct API call to getPalletByRfid endpoint
        Call<PalletListResponse.SinglePalletResponse> call = apiService.getPalletByRfid(rfidTag);
        call.enqueue(new Callback<PalletListResponse.SinglePalletResponse>() {
            @Override
            public void onResponse(Call<PalletListResponse.SinglePalletResponse> call, Response<PalletListResponse.SinglePalletResponse> response) {
                showLoading(false);
                
                Log.d("RFID", "getPalletByRfid response - Success: " + response.isSuccessful() + 
                        ", Code: " + response.code() + ", Body: " + (response.body() != null));
                
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // Pallet found - direct open
                    PalletListResponse.PalletData pallet = response.body().getData();
                    
                    Log.d("RFID", "Found pallet: " + pallet.getCode() + " for RFID: " + rfidTag);
                    Toast.makeText(getContext(), "Opening pallet: " + pallet.getCode(), Toast.LENGTH_SHORT).show();
                    
                    // Auto-open the pallet for editing using the standard method
                    openEditPalletDialog(pallet);
                    
                } else {
                    // No pallet found with this RFID tag
                    Log.w("RFID", "No pallet found for RFID tag: " + rfidTag + 
                            " - Response code: " + response.code() + 
                            ", Message: " + response.message() +
                            ", Body: " + (response.body() != null ? "present" : "null") +
                            ", Data: " + (response.body() != null && response.body().getData() != null ? "present" : "null"));
                    
                    Toast.makeText(getContext(), "No pallet found with RFID: " + rfidTag, Toast.LENGTH_LONG).show();
                    
                    // Optional: Update search field to show the scanned tag for manual search
                    etSearch.setText(rfidTag);
                    currentSearchQuery = rfidTag;
                    currentPage = 0;
                }
            }

            @Override
            public void onFailure(Call<PalletListResponse.SinglePalletResponse> call, Throwable t) {
                showLoading(false);
                Log.e("RFID", "Network error getting pallet by RFID: " + rfidTag, t);
                showError("Network error: " + t.getMessage());
                
                // Optional: Update search field as fallback
                etSearch.setText(rfidTag);
                currentSearchQuery = rfidTag;
                currentPage = 0;
            }
        });
    }

    // RFID key event handlers for MainActivity
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        Log.d("RFID", "=== KEY DOWN EVENT ===");
        Log.d("RFID", "KeyCode: " + keyCode + " (293/294/10036 = scan triggers, " + android.view.KeyEvent.KEYCODE_VOLUME_UP + "/" + android.view.KeyEvent.KEYCODE_VOLUME_DOWN + " = volume keys)");
        Log.d("RFID", "RFID Status - Connected: " + isRfidConnected + ", Scanning: " + isRfidScanning + ", Reader: " + (reader != null ? "available" : "null"));
        
        // Check for proper Zebra scan trigger buttons
        if (keyCode == 293 || keyCode == 294 || keyCode == 103 || 
            keyCode == 139 || keyCode == 280 || keyCode == 10036) {// Left and right scan trigger buttons + MC3300R specific code
            Log.d("RFID", "✓ Zebra scan trigger button detected: " + keyCode);
            if (isRfidConnected && !isRfidScanning) {
                Log.d("RFID", "✓ Starting RFID scan via hardware trigger");
                Toast.makeText(getContext(), "RFID Scan triggered - scanning...", Toast.LENGTH_SHORT).show();
                startRfidScanning();
                return true;
            } else if (!isRfidConnected) {
                Log.w("RFID", "✗ Cannot scan - RFID reader not connected");
                Toast.makeText(getContext(), "RFID reader not connected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (isRfidScanning) {
                Log.w("RFID", "✗ Cannot scan - RFID scan already in progress");
                Toast.makeText(getContext(), "RFID scan already in progress", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        
        // Also support volume keys as fallback
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP || keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) {
            Log.d("RFID", "✓ Volume key detected: " + keyCode);
            if (isRfidConnected && !isRfidScanning) {
                Log.d("RFID", "✓ Starting RFID scan via volume key");
                Toast.makeText(getContext(), "RFID Scan triggered (volume) - scanning...", Toast.LENGTH_SHORT).show();
                startRfidScanning();
                return true;
            } else {
                Log.w("RFID", "✗ Cannot scan via volume key - Connected: " + isRfidConnected + ", Scanning: " + isRfidScanning);
            }
        }
        
        Log.d("RFID", "Key not handled by RFID system: " + keyCode);
        return false;
    }

    public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        // Stop scanning when scan trigger key is released
        if (keyCode == 293 || keyCode == 294 || keyCode == 10036) { // Zebra scan trigger buttons + MC3300R specific
            if (isRfidConnected && isRfidScanning) {
                Log.d("RFID", "Hardware scan trigger released: " + keyCode);
                stopRfidScanning();
                return true;
            }
        }
        
        // Also support volume keys as fallback
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP || keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (isRfidConnected && isRfidScanning) {
                Log.d("RFID", "Volume key scan trigger released: " + keyCode);
                stopRfidScanning();
                return true;
            }
        }
        return false;
    }

    private void startRfidScanning() {
        if (reader != null && isRfidConnected && !isRfidScanning) {
            try {
                // Check if Actions and Inventory are properly initialized
                if (reader.Actions == null) {
                    Log.e("RFID", "RFID reader Actions is null");
                    Toast.makeText(getContext(), "RFID scanner Actions not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (reader.Actions.Inventory == null) {
                    Log.e("RFID", "RFID reader Inventory is null");
                    Toast.makeText(getContext(), "RFID scanner Inventory not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (isRfidScanning) {
                    Log.w("RFID", "RFID scan already in progress");
                    return;
                }
                
                // Show feedback to user
                Toast.makeText(getContext(), "RFID Scanner activated - Hold device near RFID tag", Toast.LENGTH_SHORT).show();
                
                // Start inventory/scanning
                reader.Actions.Inventory.perform();
                isRfidScanning = true;
                
                Log.d("RFID", "RFID inventory started");
                
                // Set timeout for scanning
                rfidTimeoutHandler.postDelayed(() -> {
                    stopRfidScanning();
                }, 5000); // 5 second timeout
                
            } catch (InvalidUsageException | OperationFailureException e) {
                Log.e("RFID", "Failed to start RFID scanning: " + e.getMessage());
                Toast.makeText(getContext(), "Failed to start RFID scan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                isRfidScanning = false;
            } catch (Exception e) {
                Log.e("RFID", "Unexpected error starting RFID scan", e);
                Toast.makeText(getContext(), "Unexpected error starting RFID scan", Toast.LENGTH_SHORT).show();
                isRfidScanning = false;
            }
        } else {
            String reason = "";
            if (reader == null) reason = "Reader not available";
            else if (!isRfidConnected) reason = "Reader not connected";
            else if (isRfidScanning) reason = "Scan already in progress";
            
            Log.w("RFID", "Cannot start RFID scanning: " + reason);
            Toast.makeText(getContext(), "Cannot start scanning: " + reason, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRfidScanning() {
        if (reader != null && isRfidConnected && isRfidScanning) {
            try {
                reader.Actions.Inventory.stop();
                isRfidScanning = false;
                rfidTimeoutHandler.removeCallbacksAndMessages(null);
                Log.d("RFID", "Stopped RFID scanning");
            } catch (InvalidUsageException | OperationFailureException e) {
                Log.e("RFID", "Failed to stop RFID scanning: " + e.getMessage());
            }
        }
    }

    // ========================
    // LIFECYCLE METHODS
    // ========================

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Cleanup RFID resources
        disconnectRfid();
        
        binding = null;
    }

    private void disconnectRfid() {
        try {
            if (reader != null && isRfidConnected) {
                if (isRfidScanning) {
                    stopRfidScanning();
                }
                reader.disconnect();
                isRfidConnected = false;
                Log.d("RFID", "RFID reader disconnected");
            }
            
            if (readers != null) {
                readers.Dispose();
                readers = null;
            }
        } catch (Exception e) {
            Log.e("RFID", "Error disconnecting RFID: " + e.getMessage());
        }
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

}