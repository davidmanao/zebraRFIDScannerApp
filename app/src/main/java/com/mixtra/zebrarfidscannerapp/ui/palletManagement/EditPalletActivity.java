package com.mixtra.zebrarfidscannerapp.ui.palletManagement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.api.ApiClient;
import com.mixtra.zebrarfidscannerapp.api.RfidApiService;
import com.mixtra.zebrarfidscannerapp.api.model.PalletDetailResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletUpdateRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPalletActivity extends AppCompatActivity {

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

        // Initialize API service
        apiService = ApiClient.getInstance(this).getApiService();

        // Get pallet data from intent
        extractIntentData();

        // Initialize views
        initializeViews();
        setupToolbar();
        setupEventListeners();

        // Populate fields with intent data or load from API
        populateFieldsFromIntent();
        loadPalletDetails();
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

        PalletUpdateRequest updateRequest = new PalletUpdateRequest(newRfidTag);

        Call<PalletDetailResponse> call = apiService.updatePalletRfidTag(palletId, updateRequest);
        call.enqueue(new Callback<PalletDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<PalletDetailResponse> call, @NonNull Response<PalletDetailResponse> response) {
                showLoading(false);
                fabSave.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
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
            public void onFailure(@NonNull Call<PalletDetailResponse> call, @NonNull Throwable t) {
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
    public void onBackPressed() {
        // Check if there are unsaved changes
        String currentRfidTag = etRfidTag.getText().toString().trim();
        String originalRfidTag = getIntent().getStringExtra(EXTRA_PALLET_RFID_TAG);
        
        if (!TextUtils.equals(currentRfidTag, originalRfidTag)) {
            // Show confirmation dialog for unsaved changes
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Are you sure you want to leave?")
                .setPositiveButton("Leave", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Stay", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
}