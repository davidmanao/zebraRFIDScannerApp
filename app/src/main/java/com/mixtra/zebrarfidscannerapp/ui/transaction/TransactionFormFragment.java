package com.mixtra.zebrarfidscannerapp.ui.transaction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.api.ApiClient;
import com.mixtra.zebrarfidscannerapp.api.RfidApiService;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionUpdateRequest;
import com.mixtra.zebrarfidscannerapp.databinding.FragmentTransactionFormBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionFormFragment extends Fragment {

    private FragmentTransactionFormBinding binding;
    private BatchItemAdapter batchItemAdapter;
    private List<PalletTransactionResponse.BatchItem> batchItems;
    private RfidApiService apiService;
    
    // Transaction data from arguments
    private int transactionId;
    private int palletId;
    private String transactionType;
    private String transactionDate;
    
    // Store current warehouse and location values for reliable access
    private String currentWarehouseCode = "";
    private String currentLocationCode = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize API service
        apiService = ApiClient.getInstance(requireContext()).getApiService();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTransactionFormBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initializeViews();
        setupClickListeners();

        return root;
    }

    private void initializeViews() {
        
        // Initialize batch items list
        batchItems = new ArrayList<>();
        
        // Add default batch items initially
        addDefaultBatchItems();

        // Setup RecyclerView
        batchItemAdapter = new BatchItemAdapter(batchItems);
        batchItemAdapter.setOnItemRemoveListener(this::removeBatchItem);
        binding.rvBatchItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBatchItems.setAdapter(batchItemAdapter);

        // Setup text change listeners for warehouse and location fields
        setupTextChangeListeners();

        // Load transaction data from arguments if available
        loadTransactionData();
    }

    private void setupTextChangeListeners() {
        // Add text change listeners to track warehouse and location values
        // This helps capture values entered by barcode scanners
        if (binding.tvWarehouse != null) {
            binding.tvWarehouse.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentWarehouseCode = s.toString().trim();
                    Log.d("TransactionForm", "Warehouse value changed: '" + currentWarehouseCode + "'");
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    currentWarehouseCode = s.toString().trim();
                }
            });
        }

        if (binding.tvLocation != null) {
            binding.tvLocation.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentLocationCode = s.toString().trim();
                    Log.d("TransactionForm", "Location value changed: '" + currentLocationCode + "'");
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    currentLocationCode = s.toString().trim();
                }
            });
        }
    }

    private void setupClickListeners() {
        // Add item FAB
        binding.fabAddItem.setOnClickListener(v -> {
            PalletTransactionResponse.BatchItem newItem = new PalletTransactionResponse.BatchItem();
            newItem.setBatchNo("");
            newItem.setQuantity(0);
            batchItems.add(newItem);
            batchItemAdapter.notifyItemInserted(batchItems.size() - 1);
        });

        // Cancel button
        binding.btnCancel.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        // Save Confirmation button
        binding.btnSaveConfirmation.setOnClickListener(v -> {
            if (transactionId > 0) {
                // Show loading state
                binding.btnSaveConfirmation.setEnabled(false);
                binding.btnSaveConfirmation.setText("Saving...");
                
                // Call API to update transaction
                updatePalletTransaction();
            } else {
                Toast.makeText(getContext(), "No transaction data to save", Toast.LENGTH_SHORT).show();
            }
        });

        // Bottom navigation clicks
        binding.bottomNavigation.findViewById(R.id.bottom_navigation).setOnClickListener(v -> {
            // Handle bottom navigation if needed
        });
    }

    private void loadTransactionData() {
        // Get transaction data from arguments
        Bundle args = getArguments();
        if (args != null) {
            // Store data in class fields for later use
            this.transactionId = args.getInt("transactionId", 0);
            this.palletId = args.getInt("palletId", 1);
            this.transactionType = args.getString("transactionType", "IN");
            this.transactionDate = args.getString("transactionDate", "");
            String palletCode = args.getString("palletCode", "");
            String rfidTag = args.getString("rfidTag", "");
            
            Log.d("TransactionForm", "Arguments received - palletCode: '" + palletCode + "', rfidTag: '" + rfidTag + "'");
            
            if (this.transactionId > 0) {
                // If we have a transaction ID, fetch the full transaction data from API
                loadFullTransactionData();
            } else {
                // Set the UI values from bundle arguments
                binding.tvTransactionType.setText(transactionType);
                binding.tvPalletRfid.setText(rfidTag != null && !rfidTag.trim().isEmpty() ? rfidTag : "");
                binding.tvPalletCode.setText(palletCode != null && !palletCode.trim().isEmpty() ? palletCode : "");
                currentWarehouseCode = "Main Warehouse";
                currentLocationCode = "Location-A1";
                binding.tvWarehouse.setText(currentWarehouseCode);
                binding.tvLocation.setText(currentLocationCode);
                
                // Format transaction date
                if (!transactionDate.isEmpty()) {
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
                        Date date = inputFormat.parse(transactionDate);
                        binding.tvTransactionDate.setText(outputFormat.format(date));
                    } catch (Exception e) {
                        binding.tvTransactionDate.setText("22 Sep 2025"); // fallback
                    }
                } else {
                    binding.tvTransactionDate.setText("22 Sep 2025"); // fallback
                }
            }
        } else {
            // Use sample data matching the design as fallback
            this.transactionId = 0;
            this.transactionType = "IN";
            this.transactionDate = "";
            this.palletId = 1;
            binding.tvTransactionDate.setText("22 Sep 2025");
            binding.tvTransactionType.setText("IN");
            binding.tvPalletRfid.setText("K4E55");
            currentWarehouseCode = "Main Warehouse";
            currentLocationCode = "bla bla";
            binding.tvWarehouse.setText(currentWarehouseCode);
            binding.tvLocation.setText(currentLocationCode);
        }
    }
    
    private void loadFullTransactionData() {
        // Show loading indicator
        binding.progressBar.setVisibility(View.VISIBLE);
        
        Log.d("TransactionForm", "Loading full transaction data for ID: " + transactionId);
        
        // Call API to get full transaction data
        Call<PalletTransactionResponse> call = apiService.getPalletTransaction(transactionId);
        
        call.enqueue(new Callback<PalletTransactionResponse>() {
            @Override
            public void onResponse(Call<PalletTransactionResponse> call, Response<PalletTransactionResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("TransactionForm", "Successfully loaded transaction data");
                    // Use the full transaction data to populate the form
                    setTransactionData(response.body());
                } else {
                    Log.e("TransactionForm", "Failed to load transaction data: " + response.message());
                    showErrorMessage("Failed to load transaction details: " + response.message());
                    // Fallback to basic UI setup
                    setBasicTransactionUI();
                }
            }
            
            @Override
            public void onFailure(Call<PalletTransactionResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e("TransactionForm", "Network error loading transaction data: " + t.getMessage());
                showErrorMessage("Network error: " + t.getMessage());
                // Fallback to basic UI setup
                setBasicTransactionUI();
            }
        });
    }
    
    private void setBasicTransactionUI() {
        // Set basic UI values when API call fails
        binding.tvTransactionType.setText(transactionType);
        currentWarehouseCode = "Main Warehouse";
        currentLocationCode = "Location-A1";
        binding.tvWarehouse.setText(currentWarehouseCode);
        binding.tvLocation.setText(currentLocationCode);
        binding.tvTransactionDate.setText("22 Sep 2025"); // fallback
        
        // Use default batch items
        addDefaultBatchItems();
    }

    public void setTransactionData(PalletTransactionResponse transaction) {
        if (transaction != null && transaction.getData() != null && binding != null) {
            PalletTransactionResponse.TransactionData data = transaction.getData();
            
            // Update class fields with API data
            this.transactionType = data.getType(); // Use 'type' field, not 'transactionType'
            this.palletId = data.getPalletId();
            this.transactionDate = data.getTransactionDate();
            
            // Set transaction details from the nested data
            binding.tvTransactionType.setText(data.getType());
            
            // Get pallet data from nested pallet object
            if (data.getPallet() != null) {
                String palletCode = data.getPallet().getCode();
                String rfidTag = data.getPallet().getRfidTag();
                
                Log.d("TransactionForm", "API response - palletCode: '" + palletCode + "', rfidTag: '" + rfidTag + "'");
                
                binding.tvPalletRfid.setText(rfidTag);
                binding.tvPalletCode.setText(palletCode); // Set the correct pallet code from API
            }
            
            // For now use static values since these might come from related objects
//            var locationName = (data.getLocation() != null && data.getLocation().getName() != null) ? data.getLocation().getName() : "";
//            var locationCode = (data.getLocation() != null && data.getLocation().getCode() != null) ? data.getLocation().getCode() : "";
//            var warehouseName = (data.getWarehouse() != null && data.getWarehouse().getName() != null) ? data.getWarehouse().getName() : "";
//            var warehouseCode = (data.getWarehouse() != null && data.getWarehouse().getCode() != null) ? data.getWarehouse().getCode() : "";
//            binding.tvWarehouse.setText(MessageFormat.format("{0}- {1}", warehouseCode, warehouseName));
//            binding.tvLocation.setText(MessageFormat.format("{0}- {1}", locationCode, locationName));
            currentWarehouseCode = data.getWarehouseText() != null ? data.getWarehouseText() : "";
            currentLocationCode = data.getLocationText() != null ? data.getLocationText() : "";
            binding.tvWarehouse.setText(currentWarehouseCode);
            binding.tvLocation.setText(currentLocationCode);

            // Format date
            try {
                String dateToFormat = data.getTransactionDate();
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(dateToFormat);
                assert date != null;
                binding.tvTransactionDate.setText(outputFormat.format(date));
            } catch (Exception e) {
                Log.e("TransactionForm", "Date parsing error: " + e.getMessage());
                // Try with the dateIn field as fallback
                try {
                    String dateToFormat = data.getDateIn();
                    if (dateToFormat != null) {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.getDefault());
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                        Date date = inputFormat.parse(dateToFormat);
                        binding.tvTransactionDate.setText(outputFormat.format(date));
                    } else {
                        binding.tvTransactionDate.setText(data.getTransactionDate());
                    }
                } catch (Exception ex) {
                    binding.tvTransactionDate.setText(data.getTransactionDate());
                }
            }

            // Load existing transaction details into batch items
            loadExistingTransactionDetails(data.getPalletTransactionDetails());
        }
    }
    
    private void loadExistingTransactionDetails(List<PalletTransactionResponse.TransactionDetail> transactionDetails) {
        if (transactionDetails != null && !transactionDetails.isEmpty()) {
            // Clear existing batch items and load from API
            batchItems.clear();
            
            Log.d("TransactionForm", "Loading " + transactionDetails.size() + " existing transaction details");
            
            for (PalletTransactionResponse.TransactionDetail detail : transactionDetails) {
                PalletTransactionResponse.BatchItem batchItem = new PalletTransactionResponse.BatchItem();
                batchItem.setBatchNo(detail.getBatchNo() != null ? detail.getBatchNo() : "");
                
                // Convert qty string to double for BatchItem
                try {
                    double quantity = detail.getQty() != null ? Double.parseDouble(detail.getQty()) : 0;
                    batchItem.setQuantity(quantity);
                } catch (NumberFormatException e) {
                    batchItem.setQuantity(0);
                    Log.w("TransactionForm", "Failed to parse quantity: " + detail.getQty());
                }
                
                batchItems.add(batchItem);
                Log.d("TransactionForm", "Loaded detail: BatchNo=" + detail.getBatchNo() + ", Qty=" + detail.getQty());
            }
            
            // If no details from API, add default empty items
            if (batchItems.isEmpty()) {
                Log.d("TransactionForm", "No valid details found, adding default items");
                addDefaultBatchItems();
            } else {
                Log.d("TransactionForm", "Successfully loaded " + batchItems.size() + " batch items from existing details");
            }
            
            // Notify adapter of data change
            if (batchItemAdapter != null) {
                batchItemAdapter.notifyDataSetChanged();
                Log.d("TransactionForm", "Adapter notified of data changes");
            }
        } else {
            // No existing details, use default empty items
            // Log.d("TransactionForm", "No existing transaction details found (null or empty), using defaults");
            // addDefaultBatchItems();
        }
    }
    
    private void addDefaultBatchItems() {
        batchItems.clear();
        
        // Add default batch items (like in the design)
        PalletTransactionResponse.BatchItem item1 = new PalletTransactionResponse.BatchItem();
        item1.setBatchNo("");
        item1.setQuantity(0);
        batchItems.add(item1);
        
        PalletTransactionResponse.BatchItem item2 = new PalletTransactionResponse.BatchItem();
        item2.setBatchNo("");
        item2.setQuantity(0);
        batchItems.add(item2);
        
        if (batchItemAdapter != null) {
            batchItemAdapter.notifyDataSetChanged();
        }
    }
    
    private void removeBatchItem(int position) {
        if (position >= 0 && position < batchItems.size()) {
            batchItems.remove(position);
            if (batchItemAdapter != null) {
                batchItemAdapter.notifyItemRemoved(position);
                batchItemAdapter.notifyItemRangeChanged(position, batchItems.size());
            }
        }
    }
    
    private void updatePalletTransaction() {
        // Sync field values before processing (handles barcode scanner input)
        syncFieldValues();
        
        // Log current batch items for debugging
        Log.d("TransactionForm", "Starting update with " + batchItems.size() + " batch items");
        for (int i = 0; i < batchItems.size(); i++) {
            PalletTransactionResponse.BatchItem item = batchItems.get(i);
            Log.d("TransactionForm", "Item " + i + ": BatchNo='" + item.getBatchNo() + "', Quantity=" + item.getQuantity());
        }
        
        // Prepare the transaction details from batch items
        List<PalletTransactionUpdateRequest.PalletTransactionDetail> details = new ArrayList<>();
        
        for (PalletTransactionResponse.BatchItem item : batchItems) {
            if (item.getBatchNo() != null && !item.getBatchNo().trim().isEmpty()) {
                String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
                
                PalletTransactionUpdateRequest.PalletTransactionDetail detail = 
                    new PalletTransactionUpdateRequest.PalletTransactionDetail(
                        transactionId, // palletTransactionId
                        item.getBatchNo(),
                        String.valueOf(item.getQuantity()),
                        "Transaction detail", // remark
                        currentTime, // dateIn
                        currentTime  // dateUp
                    );
                details.add(detail);
                Log.d("TransactionForm", "Added detail: BatchNo=" + item.getBatchNo() + ", Quantity=" + item.getQuantity());
            }
        }
        
        Log.d("TransactionForm", "Total details prepared: " + details.size());
        
        // If no batch items with data, add at least one default detail
        // if (details.isEmpty()) {
        //     String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
        //     PalletTransactionUpdateRequest.PalletTransactionDetail detail = 
        //         new PalletTransactionUpdateRequest.PalletTransactionDetail(
        //             transactionId,
        //             "DEFAULT_BATCH",
        //             "1",
        //             "Default transaction detail",
        //             currentTime,
        //             currentTime
        //         );
        //     details.add(detail);
        //     Log.d("TransactionForm", "No user data found, added default detail");
        // }
        
        // Create the update request
        String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
        
        // Get warehouse and location values using robust method
        String warehouseCode = getCurrentWarehouseCode();
        String locationCode = getCurrentLocationCode();
        
        Log.d("TransactionForm", "Building request with warehouse: '" + warehouseCode + "', location: '" + locationCode + "'");
        
        PalletTransactionUpdateRequest request = new PalletTransactionUpdateRequest(
            transactionId,
            palletId,
            !transactionDate.isEmpty() ? transactionDate : currentTime,
            "CONFIRMED", // status
            transactionType.equals("IN") ? "Receiving" : "Shipping", // transactionType
            transactionType, // type
            "Transaction confirmed via mobile app", // remark
            warehouseCode, // warehouseCode
            locationCode, // locationCode
            details
        );
        
        // Make API call
        Call<PalletTransactionResponse> call = apiService.updatePalletTransaction(request);
        
        call.enqueue(new Callback<PalletTransactionResponse>() {
            @Override
            public void onResponse(Call<PalletTransactionResponse> call, Response<PalletTransactionResponse> response) {
                // Reset button state
                binding.btnSaveConfirmation.setEnabled(true);
                binding.btnSaveConfirmation.setText("Save Confirmation");
                
                if (response.isSuccessful() && response.body() != null) {
                    // Success
                    Toast.makeText(getContext(), "Transaction confirmed successfully!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).popBackStack(R.id.nav_home, false);
                } else {
                    // Error
                    showErrorMessage("Failed to confirm transaction: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<PalletTransactionResponse> call, Throwable t) {
                // Reset button state
                binding.btnSaveConfirmation.setEnabled(true);
                binding.btnSaveConfirmation.setText("Save Confirmation");
                
                // Network error
                showErrorMessage("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Sync field values from UI controls to ensure we have the latest values
     * This is especially important for barcode scanner input
     */
    private void syncFieldValues() {
        try {
            if (binding != null && binding.tvWarehouse != null) {
                String warehouseText = binding.tvWarehouse.getText() != null ? 
                    binding.tvWarehouse.getText().toString().trim() : "";
                if (!warehouseText.isEmpty()) {
                    currentWarehouseCode = warehouseText;
                    Log.d("TransactionForm", "Synced warehouse: '" + currentWarehouseCode + "'");
                }
            }
            
            if (binding != null && binding.tvLocation != null) {
                String locationText = binding.tvLocation.getText() != null ? 
                    binding.tvLocation.getText().toString().trim() : "";
                if (!locationText.isEmpty()) {
                    currentLocationCode = locationText;
                    Log.d("TransactionForm", "Synced location: '" + currentLocationCode + "'");
                }
            }
        } catch (Exception e) {
            Log.w("TransactionForm", "Error syncing field values: " + e.getMessage());
        }
    }
    
    /**
     * Robust method to get current warehouse code that works with barcode scanners
     */
    private String getCurrentWarehouseCode() {
        // First try to get from our tracked variable
        if (currentWarehouseCode != null && !currentWarehouseCode.trim().isEmpty()) {
            Log.d("TransactionForm", "Using tracked warehouse code: '" + currentWarehouseCode + "'");
            return currentWarehouseCode.trim();
        }
        
        // Fallback: try to get directly from the EditText
        if (binding != null && binding.tvWarehouse != null) {
            try {
                // Force refresh of the text content
                binding.tvWarehouse.clearFocus();
                String textValue = binding.tvWarehouse.getText() != null ? 
                    binding.tvWarehouse.getText().toString().trim() : "";
                
                if (!textValue.isEmpty()) {
                    currentWarehouseCode = textValue; // Update our tracking variable
                    Log.d("TransactionForm", "Got warehouse from EditText: '" + textValue + "'");
                    return textValue;
                }
            } catch (Exception e) {
                Log.w("TransactionForm", "Error getting warehouse text: " + e.getMessage());
            }
        }
        
        // Final fallback
        Log.w("TransactionForm", "Using fallback warehouse code");
        return "Main Warehouse";
    }
    
    /**
     * Robust method to get current location code that works with barcode scanners
     */
    private String getCurrentLocationCode() {
        // First try to get from our tracked variable
        if (currentLocationCode != null && !currentLocationCode.trim().isEmpty()) {
            Log.d("TransactionForm", "Using tracked location code: '" + currentLocationCode + "'");
            return currentLocationCode.trim();
        }
        
        // Fallback: try to get directly from the EditText
        if (binding != null && binding.tvLocation != null) {
            try {
                // Force refresh of the text content
                binding.tvLocation.clearFocus();
                String textValue = binding.tvLocation.getText() != null ? 
                    binding.tvLocation.getText().toString().trim() : "";
                
                if (!textValue.isEmpty()) {
                    currentLocationCode = textValue; // Update our tracking variable
                    Log.d("TransactionForm", "Got location from EditText: '" + textValue + "'");
                    return textValue;
                }
            } catch (Exception e) {
                Log.w("TransactionForm", "Error getting location text: " + e.getMessage());
            }
        }
        
        // Final fallback
        Log.w("TransactionForm", "Using fallback location code");
        return "Location-A1";
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