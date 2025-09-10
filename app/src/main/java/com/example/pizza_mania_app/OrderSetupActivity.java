package com.example.pizza_mania_app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.helperClasses.GoogleMapsHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;

public class OrderSetupActivity extends AppCompatActivity {

    private RadioGroup radioGroupOrderType;
    private RadioButton radioDelivery, radioPickup;
    private LinearLayout layoutDelivery, layoutPickup;
    private TextView tvDeliveryAddress;
    private Button btnChangeAddress, btnContinue;
    private Spinner spinnerBranch;

    private String userId, userRole;
    private int defaultBranchId, selectedBranchId;
    private String defaultAddress, selectedAddress;
    private LatLng selectedLatLng;

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_setup);

        // Initialize Places API
        GoogleMapsHelper.initPlaces(this);

        // Intent extras
        userId = getIntent().getStringExtra("userId");
        userRole = getIntent().getStringExtra("userRole");
        defaultBranchId = getIntent().getIntExtra("defaultBranchId", -1);
        defaultAddress = getIntent().getStringExtra("defaultAddress");

        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        // Bind views
        radioGroupOrderType = findViewById(R.id.radioGroupOrderType);
        radioDelivery = findViewById(R.id.radioDelivery);
        radioPickup = findViewById(R.id.radioPickup);
        layoutDelivery = findViewById(R.id.layoutDelivery);
        layoutPickup = findViewById(R.id.layoutPickup);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);
        btnContinue = findViewById(R.id.btnContinue);
        spinnerBranch = findViewById(R.id.spinnerBranch);

        // Pre-fill default values
        if (defaultAddress != null) tvDeliveryAddress.setText(defaultAddress);
        selectedBranchId = defaultBranchId;
        selectedAddress = defaultAddress;
        selectedLatLng = GoogleMapsHelper.geocodeAddress(this, defaultAddress);

        // Branch spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.branches, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBranch.setAdapter(adapter);

        // Toggle UI
        radioGroupOrderType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioDelivery) {
                layoutDelivery.setVisibility(View.VISIBLE);
                layoutPickup.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioPickup) {
                layoutDelivery.setVisibility(View.GONE);
                layoutPickup.setVisibility(View.VISIBLE);
            }
        });

        // Places picker
        btnChangeAddress.setOnClickListener(v -> GoogleMapsHelper.openPlacePicker(this));

        // Continue → Menu
        btnContinue.setOnClickListener(v -> {
            if (radioDelivery.isChecked()) {
                selectedAddress = tvDeliveryAddress.getText().toString();
                if (selectedAddress.isEmpty()) {
                    Toast.makeText(this, "Please select a delivery address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get branch from address
                selectedBranchId = GoogleMapsHelper.getBranchIdFromAddress(this, selectedAddress);
                if (selectedBranchId == -1) return;

                // Get LatLng
                selectedLatLng = GoogleMapsHelper.geocodeAddress(this, selectedAddress);
                if (selectedLatLng == null) {
                    Toast.makeText(this, "Unable to get location coordinates", Toast.LENGTH_SHORT).show();
                    return;
                }

                launchMenu("Delivery", selectedBranchId, selectedAddress, selectedLatLng);

            } else if (radioPickup.isChecked()) {
                String branchName = spinnerBranch.getSelectedItem().toString();
                selectedBranchId = getBranchIdByName(branchName);
                launchMenu("Pickup", selectedBranchId, null, null);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GoogleMapsHelper.getAutocompleteRequestCode() && resultCode == RESULT_OK) {
            Place place = GoogleMapsHelper.getPlaceFromIntent(data);
            if (place != null && place.getAddress() != null) {
                tvDeliveryAddress.setText(place.getAddress());
                selectedAddress = place.getAddress();
                selectedLatLng = place.getLatLng();
            }
        }
    }

    private void launchMenu(String orderType, int branchId, String address, LatLng latLng) {
        Intent intent = new Intent(OrderSetupActivity.this, menu.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userRole", userRole);
        intent.putExtra("branchId", branchId);
        intent.putExtra("orderType", orderType);

        if (orderType.equals("Delivery")) {
            intent.putExtra("defaultAddress", address);
            intent.putExtra("latitude", latLng.latitude);
            intent.putExtra("longitude", latLng.longitude);
        } else {
            String branchName = getBranchNameById(branchId);
            intent.putExtra("branch", branchName);
            intent.putExtra("defaultAddress", address);
            intent.putExtra("latitude", latLng.latitude);
            intent.putExtra("longitude", latLng.longitude);
        }

        startActivity(intent);
    }

    private int getBranchIdByName(String branchName) {
        int branchId = -1;
        Cursor cursor = db.rawQuery("SELECT branch_id FROM branches WHERE branch_name=?", new String[]{branchName});
        if (cursor.moveToFirst()) branchId = cursor.getInt(0);
        cursor.close();
        return branchId;
    }

    private String getBranchNameById(int branchId) {
        String branchName = "";
        Cursor cursor = db.rawQuery("SELECT branch_name FROM branches WHERE branch_id=?", new String[]{String.valueOf(branchId)});
        if (cursor.moveToFirst()) branchName = cursor.getString(0);
        cursor.close();
        return branchName;
    }
}
