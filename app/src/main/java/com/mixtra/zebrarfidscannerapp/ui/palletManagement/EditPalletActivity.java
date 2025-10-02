package com.mixtra.zebrarfidscannerapp.ui.palletManagement;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.api.ApiClient;
import com.mixtra.zebrarfidscannerapp.api.RfidApiManager;
import com.mixtra.zebrarfidscannerapp.api.RfidApiService;
import com.mixtra.zebrarfidscannerapp.api.model.ApiResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletDetailResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletUpdateRequest;
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

public class EditPalletActivity extends AppCompatActivity implements RfidEventsListener {

    private static final String TAG = "EditPalletActivity";
    private static final String EXTRA_PALLET_ID = "extra_pallet_id";
    private static final String EXTRA_PALLET_CODE = "extra_pallet_code";
    private static final String EXTRA_PALLET_NAME = "extra_pallet_name";
    private static final String EXTRA_PALLET_TYPE = "extra_pallet_type";
    private static final String EXTRA_PALLET_CAPACITY = "extra_pallet_capacity";
    private static final String EXTRA_PALLET_TARE = "extra_pallet_tare";
    private static final String EXTRA_PALLET_INITIAL = "extra_pallet_initial";
    private static final String EXTRA_PALLET_INCOMING = "extra_pallet_incoming";
    private static final String EXTRA_PALLET_OUTGOING = "extra_pallet_outgoing";
    private static final String EXTRA_PALLET_BALANCE = "extra_pallet_balance";
    private static final String EXTRA_PALLET_RFID_TAG = "extra_pallet_rfid_tag";

    public static final String RESULT_EXTRA_UPDATED_RFID_TAG = "result_updated_rfid_tag";

    private RfidApiService apiService;
    private Readers readers;
    private RFIDReader reader;
    private boolean isConnected = false;
    private boolean isScanning = false;
    private RfidApiManager rfidApiManager;
    private int palletId;

    // UI Components
    private MaterialToolbar toolbar;
    private TextInputEditText etPalletCode;
    private TextInputEditText etPalletName;
    private TextInputEditText etPalletType;
    private TextInputEditText etCapacity;
    private TextInputEditText etTare;
    private TextInputEditText etBalance;
    private TextInputEditText etInitial;
    private TextInputEditText etIncoming;
    private TextInputEditText etOutgoing;
    private TextInputEditText etRfidTag;
    private ExtendedFloatingActionButton fabSave;
    private ProgressBar progressBar;

    public static Intent createIntent(Context context, int palletId, String code, String name, 
                                    String type, double capacity, double tare, double initial,
                                      double incoming, double outgoing, double balance, String rfidTag) {
        Intent intent = new Intent(context, EditPalletActivity.class);
        intent.putExtra(EXTRA_PALLET_ID, palletId);
        intent.putExtra(EXTRA_PALLET_CODE, code);
        intent.putExtra(EXTRA_PALLET_NAME, name);
        intent.putExtra(EXTRA_PALLET_TYPE, type);
        intent.putExtra(EXTRA_PALLET_CAPACITY, capacity);
        intent.putExtra(EXTRA_PALLET_TARE, tare);
        intent.putExtra(EXTRA_PALLET_INITIAL, initial);
        intent.putExtra(EXTRA_PALLET_INCOMING, incoming);
        intent.putExtra(EXTRA_PALLET_OUTGOING, outgoing);
        intent.putExtra(EXTRA_PALLET_BALANCE, balance);
        intent.putExtra(EXTRA_PALLET_RFID_TAG, rfidTag);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pallet);
        etRfidTag = findViewById(R.id.et_rfid_tag);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if there are unsaved changes
                String currentRfidTag = etRfidTag.getText().toString().trim();
                String originalRfidTag = getIntent().getStringExtra(EXTRA_PALLET_RFID_TAG);

                if (!TextUtils.equals(currentRfidTag, originalRfidTag)) {
                    // Show confirmation dialog for unsaved changes
                    new androidx.appcompat.app.AlertDialog.Builder(EditPalletActivity.this)
                            .setTitle("Unsaved Changes")
                            .setMessage("You have unsaved changes. Are you sure you want to leave?")
                            .setPositiveButton("Leave", (dialog, which) -> {
                                // Allow back press after confirmation
                                setEnabled(false); // Disable callback to avoid recursion
                                getOnBackPressedDispatcher().onBackPressed();
                            })
                            .setNegativeButton("Stay", null)
                            .show();
                } else {
                    // No changes → just go back
                    setEnabled(false); // disable to avoid infinite loop
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
        // Initialize API service
        apiService = ApiClient.getInstance(this).getApiService();
        
        // Initialize RFID API manager for scanner functionality
        rfidApiManager = new RfidApiManager(this);

        // Get pallet data from intent
        extractIntentData();

        // Initialize views
        initializeViews();
        setupToolbar();
        setupEventListeners();

        // Populate fields with intent data or load from API
        populateFieldsFromIntent();
        loadPalletDetails();
        
        // Make activity focusable to receive key events from RFID scanner
        getWindow().getDecorView().setFocusableInTouchMode(true);
        getWindow().getDecorView().requestFocus();
        
        // Initialize RFID reader
        initializeRfidReader();
    }

    /**
     * Initialize Zebra RFID reader and establish connection
     */
    private void initializeRfidReader() {
        Log.d(TAG, "Initializing RFID reader...");
        
        try {
            // Initialize readers list
            if (readers == null) {
                readers = new Readers(this, ENUM_TRANSPORT.ALL);
            }
            
            // Get available readers
            if (readers.GetAvailableRFIDReaderList().size() > 0) {
                // Get first available reader (usually the built-in reader)
                ReaderDevice readerDevice = readers.GetAvailableRFIDReaderList().get(0);
                reader = readerDevice.getRFIDReader();
                
                Log.d(TAG, "Found RFID reader: " + readerDevice.getName());
                
                // Connect to the reader
                connectToReader();
            } else {
                Log.w(TAG, "No RFID readers found");
                Toast.makeText(this, "No RFID scanner detected", Toast.LENGTH_LONG).show();
            }
            
        } catch (InvalidUsageException e) {
            Log.e(TAG, "Invalid usage exception during RFID initialization", e);
            Toast.makeText(this, "RFID initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Connect to the RFID reader
     */
    private void connectToReader() {
        if (reader != null && !isConnected) {
            try {
                Log.d(TAG, "Connecting to RFID reader...");
                reader.connect();
                
                // Set up event listener
                reader.Events.addEventsListener(this);
                
                // Enable events
                reader.Events.setReaderDisconnectEvent(true);
                reader.Events.setInventoryStartEvent(true);
                reader.Events.setInventoryStopEvent(true);
                reader.Events.setTagReadEvent(true);
                
                isConnected = true;
                Log.d(TAG, "RFID reader connected successfully");
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "RFID Scanner ready", Toast.LENGTH_SHORT).show();
                });
                
            } catch (InvalidUsageException | OperationFailureException e) {
                Log.e(TAG, "Failed to connect to RFID reader", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to connect to RFID scanner: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        palletId = intent.getIntExtra(EXTRA_PALLET_ID, -1);
        
        if (palletId == -1) {
            Toast.makeText(this, "Invalid pallet data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etPalletCode = findViewById(R.id.et_pallet_code);
        etPalletName = findViewById(R.id.et_pallet_name);
        etPalletType = findViewById(R.id.et_pallet_type);
        etCapacity = findViewById(R.id.et_capacity);
        etTare = findViewById(R.id.et_tare);
        etBalance = findViewById(R.id.et_balance);
        etInitial = findViewById(R.id.et_initial);
        etIncoming = findViewById(R.id.et_incoming);
        etOutgoing = findViewById(R.id.et_outgoing);
        etRfidTag = findViewById(R.id.et_rfid_tag);
        fabSave = findViewById(R.id.fab_save);
        progressBar = findViewById(R.id.progress_bar);
        
        // Make RFID tag field read-only - can only be updated by RFID scanner
        etRfidTag.setFocusable(false);
        etRfidTag.setClickable(false);
        etRfidTag.setHint("Use RFID scanner to populate this field");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupEventListeners() {
        fabSave.setOnClickListener(v -> {
            String newRfidTag = etRfidTag.getText().toString().trim();
            if (TextUtils.isEmpty(newRfidTag)) {
                etRfidTag.setError("RFID Tag cannot be empty");
                etRfidTag.requestFocus();
                return;
            }
            updatePalletRfidTag(newRfidTag);
        });
    }

    /**
     * Handle RFID scan results and update the RFID tag field
     */
    private void handleRfidScanResult(String rfidTag) {
        Log.d(TAG, "RFID tag scanned: " + rfidTag);
        
        if (rfidTag != null && !rfidTag.trim().isEmpty()) {
            // Clean up the tag ID (remove spaces, convert to uppercase)
            String cleanTag = rfidTag.trim().toUpperCase();
            
            // Update the RFID tag field with the scanned value
            etRfidTag.setText(cleanTag);
            
            // Show success feedback with vibration
            Toast.makeText(this, "RFID Tag scanned successfully: " + cleanTag, Toast.LENGTH_SHORT).show();
            
            // Optional: Add vibration feedback
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(200); // 200ms vibration
            }
            
            // Optional: Auto-save if desired (uncomment to enable)
            // updatePalletRfidTag(cleanTag);
            
        } else {
            Log.w(TAG, "Empty or null RFID tag received");
            Toast.makeText(this, "Failed to scan RFID tag - please try again", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Initiate RFID scanning when handheld trigger is pressed
     */
    private void startRfidScan() {
        Log.d(TAG, "Starting RFID scan...");
        
        if (!isConnected || reader == null) {
            Log.w(TAG, "RFID reader not connected");
            Toast.makeText(this, "RFID scanner not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isScanning) {
            Log.w(TAG, "RFID scan already in progress");
            return;
        }
        
        try {
            // Show feedback to user
            Toast.makeText(this, "RFID Scanner activated - Hold device near RFID tag", Toast.LENGTH_SHORT).show();
            
            // Start inventory/scanning
            reader.Actions.Inventory.perform();
            isScanning = true;
            
            Log.d(TAG, "RFID inventory started");
            
            // Auto-stop scanning after 5 seconds
            new android.os.Handler().postDelayed(() -> {
                stopRfidScan();
            }, 5000);
            
        } catch (InvalidUsageException | OperationFailureException e) {
            Log.e(TAG, "Failed to start RFID scan", e);
            Toast.makeText(this, "Failed to start RFID scan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isScanning = false;
        }
    }
    
    /**
     * Stop RFID scanning
     */
    private void stopRfidScan() {
        if (reader != null && isScanning) {
            try {
                reader.Actions.Inventory.stop();
                isScanning = false;
                Log.d(TAG, "RFID inventory stopped");
            } catch (InvalidUsageException | OperationFailureException e) {
                Log.e(TAG, "Failed to stop RFID scan", e);
            }
        }
    }


    private void populateFieldsFromIntent() {
        Intent intent = getIntent();
        etPalletCode.setText(intent.getStringExtra(EXTRA_PALLET_CODE));
        etPalletName.setText(intent.getStringExtra(EXTRA_PALLET_NAME));
        etPalletType.setText(intent.getStringExtra(EXTRA_PALLET_TYPE));
        etCapacity.setText(String.valueOf(intent.getIntExtra(EXTRA_PALLET_CAPACITY, 0)));
        etTare.setText(String.valueOf(intent.getIntExtra(EXTRA_PALLET_TARE, 0)));
        etBalance.setText(String.valueOf(intent.getIntExtra(EXTRA_PALLET_BALANCE, 0)));
        etInitial.setText(String.valueOf(intent.getIntExtra(EXTRA_PALLET_INITIAL, 0)));
        etIncoming.setText(String.valueOf(intent.getIntExtra(EXTRA_PALLET_INCOMING, 0)));
        etOutgoing.setText(String.valueOf(intent.getIntExtra(EXTRA_PALLET_OUTGOING, 0)));
        etRfidTag.setText(intent.getStringExtra(EXTRA_PALLET_RFID_TAG));
    }

    private void loadPalletDetails() {
        showLoading(true);

        Call<PalletDetailResponse> call = apiService.getPalletDetail(palletId);
        call.enqueue(new Callback<PalletDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<PalletDetailResponse> call, @NonNull Response<PalletDetailResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    populateFields(response.body().getData());
                } else {
                    Log.w(TAG, "Failed to load detailed pallet data, using intent data instead");
                    Toast.makeText(EditPalletActivity.this, "Using basic pallet information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PalletDetailResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to load pallet details", t);
                Toast.makeText(EditPalletActivity.this, "Failed to load complete pallet data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(PalletDetailResponse.PalletDetail detail) {
        etPalletCode.setText(detail.getCode());
        etPalletName.setText(detail.getName());
        etPalletType.setText(detail.getType());
        etCapacity.setText(String.valueOf(detail.getCapacity()));
        etTare.setText(String.valueOf(detail.getTare()));
        etBalance.setText(String.valueOf(detail.getBalance()));
        etInitial.setText(String.valueOf(detail.getInitial()));
        etIncoming.setText(String.valueOf(detail.getIncoming()));
        etOutgoing.setText(String.valueOf(detail.getOutgoing()));
        etRfidTag.setText(detail.getRfidTag() != null ? detail.getRfidTag() : "");
        
        // Update toolbar title with pallet code
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit " + detail.getCode());
        }
    }

    private void updatePalletRfidTag(String newRfidTag) {
        showLoading(true);
        fabSave.setEnabled(false);

        PalletUpdateRequest updateRequest = new PalletUpdateRequest(palletId, newRfidTag);

        Call<ApiResponse<PalletDetailResponse>> call = apiService.updatePallet(updateRequest);
        call.enqueue(new Callback<ApiResponse<PalletDetailResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PalletDetailResponse>> call, @NonNull Response<ApiResponse<PalletDetailResponse>> response) {
                showLoading(false);
                fabSave.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    // Check if data is null (error case with resultMsg)
                    if (response.body().getData() == null) {
                        String errorMsg = response.body().getResultMsg() != null 
                            ? response.body().getResultMsg() 
                            : "Update failed - no data returned";
                        
                        Log.e(TAG, "Update failed - data is null: " + errorMsg);
                        showErrorAlert("Update Failed", errorMsg);
                        return;
                    }
                    
                    // Success case - data is not null
                    Toast.makeText(EditPalletActivity.this, "RFID Tag updated successfully", Toast.LENGTH_SHORT).show();
                    
                    // Return result with updated RFID tag
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(RESULT_EXTRA_UPDATED_RFID_TAG, newRfidTag);
                    setResult(RESULT_OK, resultIntent);
                    
                    finish();
                } else {
                    String errorMsg = getErrorMessage(response.code());
                    Toast.makeText(EditPalletActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to update pallet: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PalletDetailResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                fabSave.setEnabled(true);
                Toast.makeText(EditPalletActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to update pallet", t);
            }
        });
    }

    private String getErrorMessage(int code) {
        switch (code) {
            case 400:
                return "Invalid RFID Tag format";
            case 404:
                return "Pallet not found";
            case 409:
                return "RFID Tag already exists";
            default:
                return "Failed to update RFID Tag";
        }
    }

    /**
     * Show error alert dialog with title and message
     */
    private void showErrorAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        fabSave.setEnabled(!show);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle Zebra RFID scanner trigger buttons
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                // Common Zebra RFID scanner trigger key codes
                case 293: // Left scan trigger button on Zebra devices
                case 294: // Right scan trigger button on Zebra devices  
                case 103: // Common RFID scan trigger
                case 139: // Alternative RFID scan trigger
                case 280: // Another possible RFID scan trigger
                case 10036: // Device-specific scan trigger (MC339R)
                    Log.d(TAG, "RFID trigger button pressed: " + keyCode);
                    startRfidScan();
                    return true; // Consume the event
                    
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    // Some devices use volume buttons as scan triggers
                    Log.d(TAG, "Volume button pressed as scan trigger: " + keyCode);
                    startRfidScan();
                    return true; // Consume the event
                    
                default:
                    // Let other keys be handled normally
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // ========================
    // RFID EVENT HANDLERS
    // ========================

    @Override
    public void eventReadNotify(RfidReadEvents rfidReadEvents) {
        // Handle RFID tag read events
        TagData[] tagDataArray = reader.Actions.getReadTags(1000); // Get up to 1000 tags
        
        if (tagDataArray != null && tagDataArray.length > 0) {
            for (TagData tagData : tagDataArray) {
                final String tagId = tagData.getTagID();
                final int rssi = tagData.getPeakRSSI();
                
                Log.d(TAG, "RFID tag read: " + tagId + " (RSSI: " + rssi + ")");
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    handleRfidScanResult(tagId);
                });
                
                // Stop scanning after first successful read
                stopRfidScan();
                break; // Only process the first tag
            }
        }
    }

    @Override
    public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
        // Handle RFID status events
        Log.d(TAG, "RFID Status Event: " + rfidStatusEvents.StatusEventData.getStatusEventType());
        
        STATUS_EVENT_TYPE eventType = rfidStatusEvents.StatusEventData.getStatusEventType();
        
        if (eventType == STATUS_EVENT_TYPE.INVENTORY_START_EVENT) {
            Log.d(TAG, "RFID inventory started");
        } else if (eventType == STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT) {
            Log.d(TAG, "RFID inventory stopped");
            isScanning = false;
        } else if (eventType == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
            Log.w(TAG, "RFID reader disconnected");
            isConnected = false;
            runOnUiThread(() -> {
                Toast.makeText(EditPalletActivity.this, "RFID scanner disconnected", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.d(TAG, "Other RFID status event: " + eventType);
        }
    }

    // ========================
    // ACTIVITY LIFECYCLE
    // ========================

    @Override
    protected void onResume() {
        super.onResume();
        
        // Reconnect to RFID reader if disconnected
        if (reader != null && !isConnected) {
            connectToReader();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Stop any ongoing scanning
        if (isScanning) {
            stopRfidScan();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up RFID reader resources
        cleanupRfidReader();
    }

    /**
     * Cleanup RFID reader resources
     */
    private void cleanupRfidReader() {
        try {
            // Stop scanning if active
            if (isScanning) {
                stopRfidScan();
            }
            
            // Remove event listener and disconnect
            if (reader != null) {
                if (isConnected) {
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
            
            isConnected = false;
            isScanning = false;
            
        } catch (InvalidUsageException | OperationFailureException e) {
            Log.e(TAG, "Error during RFID cleanup", e);
        }
    }

}