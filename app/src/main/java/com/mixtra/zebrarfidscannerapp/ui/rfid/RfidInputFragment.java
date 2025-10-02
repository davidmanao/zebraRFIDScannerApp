package com.mixtra.zebrarfidscannerapp.ui.rfid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.api.ApiClient;
import com.mixtra.zebrarfidscannerapp.api.RfidApiService;
import com.mixtra.zebrarfidscannerapp.api.model.PalletCodeRequest;
import com.mixtra.zebrarfidscannerapp.api.model.PalletListResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionResponse;
import com.mixtra.zebrarfidscannerapp.api.models.PalletMaster;
import com.mixtra.zebrarfidscannerapp.databinding.FragmentRfidInputBinding;
import com.mixtra.zebrarfidscannerapp.utils.ScannerListener;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RfidInputFragment extends Fragment implements ScannerListener, RfidEventsListener {

    private static final String TAG = "RfidInputFragment";
    private FragmentRfidInputBinding binding;
    private RfidApiService apiService;
    private boolean isBarcodeMode = false; // Default to RFID mode
    private boolean isRfidMode = true; // Default to RFID mode
    private ProgressDialog progressDialog;
    
    // RFID Scanner variables
    private Readers readers;
    private RFIDReader reader;
    private boolean isRfidConnected = false;
    private boolean isRfidScanning = false;
    
    // BroadcastReceiver for listening to barcode scans from DataWedge
    private final BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Only process barcode intents in barcode mode
            if (!isBarcodeMode) {
                Log.d("BarcodeScanner", "Barcode received but in manual mode - ignoring");
                return;
            }
            
            String action = intent.getAction();
            Log.d("BarcodeScanner", "Intent received: " + action);
            
            // Log all extras to debug what DataWedge is sending
            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    Log.d("BarcodeScanner", "Extra: " + key + " = " + value);
                }
            }
            
            // Check for DataWedge intent actions
            String barcode = null;
            String labelType = null;
            
            if (action != null) {
                // Try different possible data keys based on common DataWedge configurations
                barcode = intent.getStringExtra("com.symbol.datawedge.data_string");
                labelType = intent.getStringExtra("com.symbol.datawedge.label_type");
                
                if (barcode == null) {
                    barcode = intent.getStringExtra("com.zebra.rfidreader.api.data_string");
                }
                if (barcode == null) {
                    barcode = intent.getStringExtra("data_string");
                }
                if (barcode == null) {
                    barcode = intent.getStringExtra("barcode_data");
                }
                
                if (barcode != null && !barcode.isEmpty()) {
                    Log.d("BarcodeScanner", "Barcode received via intent: " + barcode + " Type: " + labelType);
                    
                    // Set the scanned barcode to the EditText
                    binding.etCodePallet.setText(barcode);
                    
                    // Show success toast
                    Toast.makeText(context, "Scanned: " + barcode, Toast.LENGTH_SHORT).show();
                } else {
                    Log.w("BarcodeScanner", "Intent received but no barcode data found");
                }
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRfidInputBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize API service
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        apiService = apiClient.getApiService();

        setupClickListeners();
        
        // Configure DataWedge on fragment creation
        initializeScanner();
        
        // Setup text watcher to detect barcode input via keyboard wedge
        setupTextWatcher();

        return root;
    }

    /**
     * Initialize Zebra RFID reader and establish connection
     */
    private void initializeRfidReader() {
        if (!isRfidMode) {
            return; // Only initialize when in RFID mode
        }
        
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
            Toast.makeText(getContext(), "RFID initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Connect to the RFID reader
     */
    private void connectToRfidReader() {
        if (reader != null && !isRfidConnected) {
            try {
                Log.d(TAG, "Connecting to RFID reader...");
                if(!reader.isConnected()) reader.connect();
                
                // Set up event listener
                reader.Events.addEventsListener(this);
                
                // Enable events
                reader.Events.setHandheldEvent(true);
                reader.Events.setReaderDisconnectEvent(true);
                reader.Events.setTagReadEvent(true);
                
                isRfidConnected = true;
                Log.d(TAG, "RFID reader connected successfully");
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "RFID Scanner ready", Toast.LENGTH_SHORT).show();
                    });
                }
                
            } catch (InvalidUsageException | OperationFailureException e) {
                Log.e(TAG, "Failed to connect to RFID reader", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to connect to RFID scanner: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
                // Retry connection with delay
                retryRfidConnection(1);
            }
        }
    }

    /**
     * Retry RFID connection with exponential backoff
     */
    private void retryRfidConnection(int attempt) {
        if (attempt > 3 || !isRfidMode) {
            Log.e(TAG, "Maximum retry attempts reached or not in RFID mode");
            return;
        }
        
        long delayMs = attempt * 2000; // 2s, 4s, 6s delays
        Log.d(TAG, "Retrying RFID connection in " + (delayMs/1000) + " seconds, attempt: " + attempt);
        
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (isRfidMode && !isRfidConnected) {
                Log.d(TAG, "Retry RFID connection attempt " + attempt);
                try {
                    initializeRfidReader();
                } catch (Exception e) {
                    Log.e(TAG, "Retry attempt " + attempt + " failed", e);
                    retryRfidConnection(attempt + 1);
                }
            }
        }, delayMs);
    }

    /**
     * Start RFID scanning when trigger is pressed
     */
    private void startRfidScan() {
        Log.d(TAG, "Starting RFID scan...");
        
        // Check if reader is properly initialized
        if (reader == null) {
            Log.w(TAG, "RFID reader is null - attempting to initialize");
            Toast.makeText(getContext(), "RFID scanner not initialized - please wait", Toast.LENGTH_SHORT).show();
            initializeRfidReader();
            return;
        }
        
        if (!isRfidConnected) {
            Log.w(TAG, "RFID reader not connected - attempting to connect");
            Toast.makeText(getContext(), "RFID scanner not connected - attempting to connect", Toast.LENGTH_SHORT).show();
            connectToRfidReader();
            return;
        }
        
        // Check if Actions and Inventory are properly initialized
        if (reader.Actions == null) {
            Log.e(TAG, "RFID reader Actions is null");
            Toast.makeText(getContext(), "RFID scanner Actions not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (reader.Actions.Inventory == null) {
            Log.e(TAG, "RFID reader Inventory is null");
            Toast.makeText(getContext(), "RFID scanner Inventory not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isRfidScanning) {
            Log.w(TAG, "RFID scan already in progress");
            return;
        }
        
        try {
            // Show feedback to user
            Toast.makeText(getContext(), "RFID Scanner activated - Hold device near RFID tag", Toast.LENGTH_SHORT).show();
            
            // Start inventory/scanning
            reader.Actions.Inventory.perform();
            isRfidScanning = true;
            
            Log.d(TAG, "RFID inventory started");
            
            // Auto-stop scanning after 5 seconds
            new android.os.Handler().postDelayed(() -> {
                stopRfidScan();
            }, 5000);
            
        } catch (InvalidUsageException | OperationFailureException e) {
            Log.e(TAG, "Failed to start RFID scan", e);
            Toast.makeText(getContext(), "Failed to start RFID scan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isRfidScanning = false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error starting RFID scan", e);
            Toast.makeText(getContext(), "Unexpected error starting RFID scan", Toast.LENGTH_SHORT).show();
            isRfidScanning = false;
        }
    }
    
    /**
     * Stop RFID scanning
     */
    private void stopRfidScan() {
        if (reader != null && isRfidScanning) {
            try {
                // Check if Actions and Inventory are available before stopping
                if (reader.Actions != null && reader.Actions.Inventory != null) {
                    reader.Actions.Inventory.stop();
                    Log.d(TAG, "RFID inventory stopped");
                } else {
                    Log.w(TAG, "Cannot stop RFID scan - Actions or Inventory is null");
                }
                isRfidScanning = false;
            } catch (InvalidUsageException | OperationFailureException e) {
                Log.e(TAG, "Failed to stop RFID scan", e);
                isRfidScanning = false;
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error stopping RFID scan", e);
                isRfidScanning = false;
            }
        }
    }

    private void setupClickListeners() {
        // Radio button group listener for input mode switching
        binding.rgInputMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_barcode_mode) {
                isBarcodeMode = true;
                isRfidMode = false;
                enableBarcodeMode();
                Log.d("RfidInput", "Switched to Barcode Mode");
            } else if (checkedId == R.id.rb_manual_mode) {
                isBarcodeMode = false;
                isRfidMode = false;
                enableManualMode();
                Log.d("RfidInput", "Switched to Manual Mode");
            } else if (checkedId == R.id.rb_rfid_mode) {
                isBarcodeMode = false;
                isRfidMode = true;
                enableRfidMode();
                Log.d("RfidInput", "Switched to RFID Mode");
            }
        });

        // Camera/QR scan area click listener
        binding.cameraSection.setOnClickListener(v -> {
            // Test different scanning approaches
            testScanningMethods();
        });

        // Add Pallet button click listener
        binding.btnAddPallet.setOnClickListener(v -> {
            String codePallet = binding.etCodePallet.getText().toString().trim();

            if (validateInputs(codePallet)) {
                createPalletTransaction(codePallet);
            }
        });
        
        // Initialize with default RFID mode
        enableRfidMode();
    }

    private void enableBarcodeMode() {
        // Show barcode section and hide manual/RFID sections
        binding.barcodeSection.setVisibility(View.VISIBLE);
        binding.manualSection.setVisibility(View.GONE);
        binding.rfidSection.setVisibility(View.GONE);
        
        // Clear any existing text when switching to barcode mode
        binding.etCodePallet.setText("");
        
        Toast.makeText(getContext(), "Barcode Mode: Use scanner button or camera", Toast.LENGTH_SHORT).show();
    }

    private void enableManualMode() {
        // Hide barcode/RFID sections and show manual section
        binding.barcodeSection.setVisibility(View.GONE);
        binding.manualSection.setVisibility(View.VISIBLE);
        binding.rfidSection.setVisibility(View.GONE);
        
        // Enable EditText for manual input and request focus
        binding.etCodePallet.requestFocus();
        
        Toast.makeText(getContext(), "Manual Mode: Type in the code field", Toast.LENGTH_SHORT).show();
    }

    private void enableRfidMode() {
        // Hide barcode/manual sections and show RFID section
        binding.barcodeSection.setVisibility(View.GONE);
        binding.manualSection.setVisibility(View.GONE);
        binding.rfidSection.setVisibility(View.VISIBLE);
        
        // Clear any existing text
        binding.etCodePallet.setText("");
        binding.etRfidTag.setText("");
        
        // Initialize RFID scanner with a slight delay to ensure UI is ready
        new android.os.Handler().postDelayed(() -> {
            initializeRfidReader();
        }, 500);
        
        Toast.makeText(getContext(), "RFID Mode: Use RFID trigger button to scan tags", Toast.LENGTH_SHORT).show();
    }
    
    private void setupTextWatcher() {
        binding.etCodePallet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (!text.isEmpty() && isBarcodeMode && !isRfidMode) {
                    Log.d("BarcodeInput", "Text detected in barcode mode: " + text);
                    // Only show alert for longer barcodes (likely scanned, not manually typed)
                    if (text.length() >= 6) {
                        showBarcodeScannedAlert(text);
                    } else {
                        Toast.makeText(getContext(), "Barcode detected: " + text, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void showRFIDScannedAlert(String rfidTag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("RfidTag Scanned")
                .setMessage("Scanned RfidTag: " + rfidTag)
                .setPositiveButton("Load Pallet", (dialog, which) -> {
                    loadPalletByRfidTag(rfidTag);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void loadPalletByRfidTag(String rfidTag) {
        Log.d(TAG, "loadPalletByRfidTag called with rfidtag: " + rfidTag);

        if (apiService == null) {
            Log.e(TAG, "apiService is null!");
            Toast.makeText(requireContext(), "API service not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog for pallet lookup
        showProgressDialog("Loading Pallet", "Searching for pallet with rfidTag: " + rfidTag);

        Log.d(TAG, "Making API call to getPalletByRfidTag: " + rfidTag);
        Call<PalletListResponse.SinglePalletResponse> call = apiService.getPalletByRfid(rfidTag);
        call.enqueue(new Callback<PalletListResponse.SinglePalletResponse>() {
            @Override
            public void onResponse(Call<PalletListResponse.SinglePalletResponse> call, Response<PalletListResponse.SinglePalletResponse> response) {
                // Hide progress dialog
                hideProgressDialog();

                Log.d(TAG, "API Response received - Success: " + response.isSuccessful() +
                        ", Code: " + response.code() + ", Body: " + (response.body() != null));

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    PalletListResponse.PalletData pallet = response.body().getData();
                    Log.d(TAG, "Pallet found: " + pallet.getCode() + " - " + pallet.getName());
                    
                    // Show alert with pallet information and confirmation to create transaction
                    showPalletFoundAlert(pallet);
                } else {
                    // Show warning when pallet not found
                    Log.w(TAG, "Pallet not found or API error - Error Code: " + response.code() +
                            ", Message: " + response.message() +
                            ", Body: " + (response.body() != null ? "present" : "null") +
                            ", Data: " + (response.body() != null && response.body().getData() != null ? "present" : "null"));

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Pallet Not Found")
                            .setMessage("No pallet found with rfidTag: " + rfidTag + "\n\nPlease check the barcode and try again.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(true)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<PalletListResponse.SinglePalletResponse> call, Throwable t) {
                // Hide progress dialog
                hideProgressDialog();

                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Error loading pallet: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // Show warning dialog for network/API errors
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Connection Error")
                        .setMessage("Failed to load pallet data for code: " + rfidTag + "\n\nError: " + t.getMessage())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(true)
                        .show();
            }
        });
    }
    private void showBarcodeScannedAlert(String barcode) {
        Log.d(TAG, "Showing barcode scanned alert for: " + barcode);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Barcode Scanned")
               .setMessage("Scanned barcode: " + barcode)
               .setPositiveButton("Load Pallet", (dialog, which) -> {
                   loadPalletByBarcode(barcode);
               })
               .setNegativeButton("Cancel", (dialog, which) -> {
                   dialog.dismiss();
               })
               .show();
    }

    /**
     * Show alert with pallet information found by RFID tag and confirmation to create transaction
     */
    private void showPalletFoundAlert(PalletListResponse.PalletData pallet) {
        Log.d(TAG, "Showing pallet found alert for: " + pallet.getCode());
        
        // Build detailed pallet information message
        StringBuilder message = new StringBuilder();
        message.append("Pallet Details:\n");
        message.append("• Code: ").append(pallet.getCode()).append("\n");
        message.append("• Name: ").append(pallet.getName()).append("\n");
        
        if (pallet.getType() != null && !pallet.getType().trim().isEmpty()) {
            message.append("• Type: ").append(pallet.getType()).append("\n");
        }
        
        if (pallet.getCapacity() != null) {
            message.append("• Capacity: ").append(pallet.getCapacity()).append("\n");
        }
        
        if (pallet.getBalance() != null) {
            message.append("• Balance: ").append(pallet.getBalance()).append("\n");
        }
        
        if (pallet.getRfidTag() != null && !pallet.getRfidTag().trim().isEmpty()) {
            message.append("• RFID Tag: ").append(pallet.getRfidTag()).append("\n");
        }
        
        if (pallet.getDateIn() != null && !pallet.getDateIn().trim().isEmpty()) {
            message.append("• Date Created: ").append(pallet.getDateIn()).append("\n");
        }
        
        message.append("\nDo you want to create a new pallet transaction?");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("✅ Pallet Found")
               .setMessage(message.toString())
               .setIcon(android.R.drawable.ic_dialog_info)
               .setPositiveButton("Create Transaction", (dialog, which) -> {
                   Log.d(TAG, "User confirmed to create transaction for pallet: " + pallet.getCode());
                   createPalletTransaction(pallet.getCode());
                   dialog.dismiss();
               })
               .setNegativeButton("Cancel", (dialog, which) -> {
                   Log.d(TAG, "User cancelled transaction creation for pallet: " + pallet.getCode());
                   dialog.dismiss();
               })
               .setCancelable(true)
               .show();
    }


    private void loadPalletByBarcode(String barcode) {
        Log.d(TAG, "loadPalletByBarcode called with barcode: " + barcode);

        if (apiService == null) {
            Log.e(TAG, "apiService is null!");
            Toast.makeText(requireContext(), "API service not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog for pallet lookup
        showProgressDialog("Loading Pallet", "Searching for pallet with code: " + barcode);

        Log.d(TAG, "Making API call to getPalletByCode: " + barcode);
        Call<PalletListResponse.SinglePalletResponse> call = apiService.getPalletByCode(barcode);
        call.enqueue(new Callback<PalletListResponse.SinglePalletResponse>() {
            @Override
            public void onResponse(Call<PalletListResponse.SinglePalletResponse> call, Response<PalletListResponse.SinglePalletResponse> response) {
                // Hide progress dialog
                hideProgressDialog();
                
                Log.d(TAG, "API Response received - Success: " + response.isSuccessful() +
                        ", Code: " + response.code() + ", Body: " + (response.body() != null));

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    PalletListResponse.PalletData pallet = response.body().getData();
                    Log.d(TAG, "Pallet found: " + pallet.getCode() + " - " + pallet.getName());
                    createPalletTransaction(pallet.getCode());
                } else {
                    // Show warning when pallet not found
                    Log.w(TAG, "Pallet not found or API error - Code: " + response.code() +
                            ", Message: " + response.message() +
                            ", Body: " + (response.body() != null ? "present" : "null") +
                            ", Data: " + (response.body() != null && response.body().getData() != null ? "present" : "null"));

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Pallet Not Found")
                            .setMessage("No pallet found with code: " + barcode + "\n\nPlease check the barcode and try again.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(true)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<PalletListResponse.SinglePalletResponse> call, Throwable t) {
                // Hide progress dialog
                hideProgressDialog();
                
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Error loading pallet: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // Show warning dialog for network/API errors
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Connection Error")
                        .setMessage("Failed to load pallet data for code: " + barcode + "\n\nError: " + t.getMessage())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(true)
                        .show();
            }
        });
    }
    private void testScanningMethods() {
        Log.d("RfidInput", "Testing scanning methods...");
        
        // Method 1: Focus EditText for keyboard wedge mode
        binding.etCodePallet.requestFocus();
        binding.etCodePallet.setText(""); // Clear field
        
        // Method 2: Configure DataWedge for intent mode
        try {
            ScannerListener.configureDataWedge(requireContext());
            Log.d("RfidInput", "DataWedge configured for intent mode");
        } catch (Exception e) {
            Log.e("RfidInput", "Failed to configure DataWedge: " + e.getMessage());
        }
        
        // Method 3: Try to trigger soft scan
        Intent softScanIntent = new Intent();
        softScanIntent.setAction("com.symbol.datawedge.api.ACTION");
        softScanIntent.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "START_SCANNING");
        requireContext().sendBroadcast(softScanIntent);
        
        Toast.makeText(getContext(), 
            "Ready for scan!\n1. Hardware button should work\n2. Try scanning a barcode now", 
            Toast.LENGTH_LONG).show();
    }

    private void configureDataWedgeAndScan() {
        try {
            // Configure DataWedge using ScannerListener
            ScannerListener.configureDataWedge(requireContext());
            
            // Show feedback to user
            Toast.makeText(getContext(), "DataWedge configured. Ready to scan!", Toast.LENGTH_SHORT).show();
            
            // Optional: Trigger a soft scan programmatically
            triggerSoftScan();
            
        } catch (Exception e) {
            Log.e("RfidInput", "Error configuring DataWedge: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error configuring scanner: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void triggerSoftScan() {
        // Send intent to trigger software scan
        Intent intent = new Intent();
        intent.setAction("com.symbol.datawedge.api.ACTION");
        intent.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "START_SCANNING");
        requireContext().sendBroadcast(intent);
        
        Log.d("RfidInput", "Soft scan triggered");
    }
    
    private void initializeScanner() {
        try {
            // Configure DataWedge when fragment is created
            ScannerListener.configureDataWedge(requireContext());
            Log.d("RfidInput", "Scanner initialized successfully");
        } catch (Exception e) {
            Log.e("RfidInput", "Failed to initialize scanner: " + e.getMessage(), e);
        }
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
        // Show loading state on button
        binding.btnAddPallet.setEnabled(false);
        binding.btnAddPallet.setText("Creating...");
        
        // Show progress dialog
        showProgressDialog("Creating Pallet Transaction", "Please wait while we process your request...");
        
        // Create request with pallet code
        PalletCodeRequest request = new PalletCodeRequest(palletCode);
        
        // Make API call to create pallet transaction
        Call<PalletTransactionResponse> call = apiService.createPalletTransactionWithCode(
            request
        );
        
        call.enqueue(new Callback<PalletTransactionResponse>() {
            @Override
            public void onResponse(Call<PalletTransactionResponse> call, Response<PalletTransactionResponse> response) {
                // Hide progress dialog and reset button state
                hideProgressDialog();
                binding.btnAddPallet.setEnabled(true);
                binding.btnAddPallet.setText("Add Pallet");
                try {
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
                            var errorMsg = transactionResponse.getResultMsg() != null ? transactionResponse.getResultMsg() : "No details provided";
                            Log.w("RfidInput", "Transaction created but no data returned in response");
                            Log.w("RfidInput", "Result message: " + errorMsg);
                            showErrorMessage("Error: " + errorMsg);
                            clearFields();
                        }
                        
                    } else {
                        Log.e("RfidInput", "Create transaction failed - Response code: " + response.code());
                        Log.e("RfidInput", "Error message: " + response.message());
                        showErrorMessage("Failed to create pallet transaction: " + response.message());
                    }
                     } catch (Exception e) {
        Log.e("RfidInput", "Exception in onResponse: " + e.getMessage(), e);
        showErrorMessage("Unexpected error occurred: " + e.getMessage());
    }
            }

            @Override
            public void onFailure(Call<PalletTransactionResponse> call, Throwable t) {
                // Hide progress dialog and reset button state
                hideProgressDialog();
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
    Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
    // Customize Toast view
            View view = toast.getView();
            if (view != null) {
                // Set background color
                view.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)); // or use a custom color

                // Set text color
                if (view instanceof android.widget.LinearLayout) {
                    android.widget.TextView text = (android.widget.TextView) ((android.widget.LinearLayout) view).getChildAt(0);
                    text.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white)); // or use a custom color
                }
            }

            // Show Toast
            toast.show();

            // Custom fade timeout (Toast duration is fixed, but you can hide it early)
            new android.os.Handler().postDelayed(toast::cancel, 5000); // 1000 ms = 1 second


    }

    private void clearFields() {
        binding.etCodePallet.setText("");
    }

    /**
     * Show progress dialog with custom title and message
     */
    private void showProgressDialog(String title, String message) {
        try {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(requireContext());
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }
            
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
            
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing progress dialog: " + e.getMessage());
        }
    }

    /**
     * Hide progress dialog
     */
    private void hideProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding progress dialog: " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        connectToRfidReader();
        
        // Register receiver for ALL possible DataWedge intents
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.symbol.datawedge.data");
        filter.addAction("com.zebra.SCAN_RESULT");
        filter.addAction("com.symbol.datawedge.api.RESULT_ACTION");
        filter.addAction("com.motorolasolutions.emdk.datawedge.data");
        
        try {
            ContextCompat.registerReceiver(requireContext(), barcodeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            Log.d("BarcodeScanner", "BroadcastReceiver registered for intent-based scanning");
        } catch (Exception e) {
            Log.e("BarcodeScanner", "Failed to register BroadcastReceiver: " + e.getMessage());
        }
        
        // Optional: Set focus to capture key events from hardware buttons
        // Comment out these lines if DataWedge keyboard mode is working fine
        if (getView() != null) {
            getView().setFocusableInTouchMode(true);
            getView().requestFocus();
//            getView().setOnKeyListener((v, keyCode, event) -> {
//                return onKeyDown(keyCode, event);
//            });
        }
    }
    
    /**
     * Handle hardware button presses (including scan trigger button)
     */
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
            // Handle RFID scanning in RFID mode
            if (isRfidMode) {
                switch (keyCode) {
                    case 293: // Left scan trigger button on Zebra devices
                    case 294: // Right scan trigger button on Zebra devices  
                    case 103: // Common RFID scan trigger
                    case 139: // Alternative RFID scan trigger
                    case 280: // Another possible RFID scan trigger
                    case 10036: // Device-specific scan trigger (MC339R)
                        Log.d("RfidTrigger", "RFID trigger button pressed: " + keyCode);
                        startRfidScan();
                        return true; // Consume the event
                }
            }
            
            // Handle barcode scanning in barcode mode (existing logic)
            if (isBarcodeMode && !isRfidMode) {
                switch (keyCode) {
                    case 10036: // Your specific Zebra device scan trigger
                        Log.d("ScanTrigger", "Hardware scan button pressed: " + keyCode);
                        handleScanTrigger();
                        return false; // Let DataWedge also handle the key event
                        
                    case android.view.KeyEvent.KEYCODE_VOLUME_UP:
                    case android.view.KeyEvent.KEYCODE_VOLUME_DOWN:
                    case 103: // Common scan trigger key code for Zebra devices
                    case 139: // Alternative scan trigger key code  
                    case 280: // Another possible scan trigger key code
                    case 293: // Left scan button on some Zebra devices
                    case 294: // Right scan button on some Zebra devices
                        Log.d("ScanTrigger", "Hardware scan button pressed: " + keyCode);
                        handleScanTrigger();
                        return false; // Let DataWedge also handle the key event
                        
                    case android.view.KeyEvent.KEYCODE_BACK:
                        // Handle back button if needed
                        Log.d("ScanTrigger", "Back button pressed");
                        return false; // Let system handle it
                        
                    default:
                        Log.d("ScanTrigger", "Key pressed: " + keyCode);
                        return false;
                }
            }
        }
        return false;
    }

    // ========================
    // RFID EVENT HANDLERS
    // ========================


    public void eventReadNotify(RfidReadEvents rfidReadEvents) {
        // Handle RFID tag read events
        if (reader != null && reader.Actions != null) {
            try {
                TagData[] tagDataArray = reader.Actions.getReadTags(1000); // Get up to 1000 tags

                Log.e(TAG, " reading RFID tags" + tagDataArray.toString());
                if (tagDataArray != null && tagDataArray.length > 0) {
                    for (TagData tagData : tagDataArray) {
                        final String tagId = tagData.getTagID();
                        final int rssi = tagData.getPeakRSSI();

                        Log.d(TAG, "RFID tag read: " + tagId + " (RSSI: " + rssi + ")");

                        // Update UI on main thread
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                handleRfidScanResult(tagId);
                            });
                        }

                        // Stop scanning after first successful read
                        stopRfidScan();
                        break; // Only process the first tag
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading RFID tags", e);
            }
        } else {
            Log.w(TAG, "Cannot read RFID tags - reader or Actions is null");
        }
    }

    @Override
    public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
        // Handle RFID status events
        Log.d(TAG, "RFID Status Event: " + rfidStatusEvents.StatusEventData.getStatusEventType());

        STATUS_EVENT_TYPE eventType = rfidStatusEvents.StatusEventData.getStatusEventType();
        if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                Log.d(TAG, "RFID inventory started");
                try {
                    reader.Actions.Inventory.perform();
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                }
            } else if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                Log.d(TAG, "RFID inventory started");
                try {
                    reader.Actions.Inventory.stop();
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                }
            }
        }else if (eventType == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
            Log.w(TAG, "RFID reader disconnected");
            isRfidConnected = false;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "RFID scanner disconnected", Toast.LENGTH_SHORT).show();
                });
            }
        } else {
            Log.d(TAG, "Other RFID status event: " + eventType);
        }
    }

    /**
     * Handle RFID scan results and update the appropriate fields
     */
    private void handleRfidScanResult(String rfidTag) {
        Log.d(TAG, "RFID tag scanned: " + rfidTag);
        
        if (rfidTag != null && !rfidTag.trim().isEmpty()) {
            // Clean up the tag ID (remove spaces, convert to uppercase)
            String cleanTag = rfidTag.trim().toUpperCase();
            
            // Update both the RFID tag field and the main code field
            binding.etRfidTag.setText(cleanTag);
            binding.etCodePallet.setText(cleanTag);
            
            // Show success feedback with vibration
            Toast.makeText(getContext(), "RFID Tag scanned: " + cleanTag, Toast.LENGTH_SHORT).show();
            
            // Optional: Add vibration feedback
            try {
                android.os.Vibrator vibrator = (android.os.Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(200); // 200ms vibration
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to vibrate", e);
            }
            showRFIDScannedAlert(rfidTag);
            // Optional: Auto-process if desired (uncomment to enable)
            // showBarcodeScannedAlert(cleanTag);
            
        } else {
            Log.w(TAG, "Empty or null RFID tag received");
            Toast.makeText(getContext(), "Failed to scan RFID tag - please try again", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void handleScanTrigger() {
        Log.d("RfidInput", "Scan trigger activated via hardware button");
        
        // Just show visual feedback - let DataWedge handle the actual scanning
        Toast.makeText(getContext(), "Scan button pressed - scanning...", Toast.LENGTH_SHORT).show();
        
        // In barcode mode, make sure the hidden EditText gets focus to receive keyboard input
        if (isBarcodeMode && binding.etCodePallet != null) {
            binding.etCodePallet.requestFocus();
            Log.d("RfidInput", "EditText focused for barcode input");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            requireContext().unregisterReceiver(barcodeReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver was not registered
            Log.w("RfidInput", "Barcode receiver not registered");
        }
        
        // Stop any ongoing RFID scanning
        if (isRfidScanning) {
            stopRfidScan();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Clean up RFID reader resources
        cleanupRfidReader();
        
        // Clean up progress dialog
        hideProgressDialog();
        progressDialog = null;
        
        binding = null;
    }

    /**
     * Cleanup RFID reader resources
     */
    private void cleanupRfidReader() {
        try {
            // Stop scanning if active
            if (isRfidScanning) {
                stopRfidScan();
            }
            
            // Remove event listener and disconnect
            if (reader != null) {
                if (isRfidConnected) {
                    reader.Events.removeEventsListener(this);
                    reader.disconnect();
                    Log.d(TAG, "RFID reader disconnected and cleaned up");
                }
                reader = null;
            }
            
            // Dispose readers
            if (readers != null) {
                readers.Dispose();
                readers = null;
            }
            
            isRfidConnected = false;
            isRfidScanning = false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error during RFID cleanup", e);
        }
    }
}