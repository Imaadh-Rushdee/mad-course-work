package com.example.pizza_mania_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.pizza_mania_app.helperClasses.SelectLocationActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

public class OrderSetupActivity extends AppCompatActivity {

    private RadioGroup radioGroupOrderType;
    private RadioButton radioDelivery, radioPickup;
    private LinearLayout layoutDelivery, layoutPickup;
    private TextView tvDeliveryAddress;
    private Button btnChangeAddress, btnContinue;
    private Spinner spinnerBranch;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int MAP_REQUEST_CODE = 102;

    private String selectedAddress = "";
    private LatLng selectedLatLng;
    private int selectedBranchId = 1;
    private String userId;

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_setup);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);
        userId = getIntent().getStringExtra("userId");

        radioGroupOrderType = findViewById(R.id.radioGroupOrderType);
        radioDelivery = findViewById(R.id.radioDelivery);
        radioPickup = findViewById(R.id.radioPickup);
        layoutDelivery = findViewById(R.id.layoutDelivery);
        layoutPickup = findViewById(R.id.layoutPickup);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);
        btnContinue = findViewById(R.id.btnContinue);
        spinnerBranch = findViewById(R.id.spinnerBranch);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.branches, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBranch.setAdapter(adapter);

        radioGroupOrderType.setOnCheckedChangeListener((group, checkedId) -> {
            layoutDelivery.setVisibility(checkedId == R.id.radioDelivery ? View.VISIBLE : View.GONE);
            layoutPickup.setVisibility(checkedId == R.id.radioPickup ? View.VISIBLE : View.GONE);

            if (checkedId == R.id.radioDelivery) getCurrentLocation();
            else if (checkedId == R.id.radioPickup) loadCustomerPickupInfo();
        });

        btnChangeAddress.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSetupActivity.this, SelectLocationActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        });

        btnContinue.setOnClickListener(v -> {
            if (radioDelivery.isChecked()) {
                if (selectedAddress.isEmpty()) {
                    Toast.makeText(this, "Please select a delivery address", Toast.LENGTH_SHORT).show();
                    return;
                }
                launchMenu("delivery", selectedBranchId, selectedAddress, selectedLatLng);
            } else if (radioPickup.isChecked()) {
                launchMenu("pickup", selectedBranchId, selectedAddress, null);
            } else {
                Toast.makeText(this, "Please select an order type", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                selectedAddress = getAddressFromLocation(location);
                tvDeliveryAddress.setText(selectedAddress);
                Toast.makeText(this, "Location detected: " + selectedAddress, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to get current location. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getAddressFromLocation(Location location) {
        String result = "";
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) result = addresses.get(0).getAddressLine(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void loadCustomerPickupInfo() {
        Cursor cursor = db.rawQuery(
                "SELECT address, branch_id FROM users WHERE user_id=? AND role='customer'",
                new String[]{userId});

        if (cursor.moveToFirst()) {
            selectedAddress = cursor.getString(0);
            int branchIdFromDb = cursor.getInt(1);
            selectedBranchId = branchIdFromDb;
            tvDeliveryAddress.setText(selectedAddress);

            String branchName = getBranchNameById(branchIdFromDb);
            ArrayAdapter adapter = (ArrayAdapter) spinnerBranch.getAdapter();
            int spinnerPosition = adapter.getPosition(branchName);
            if (spinnerPosition >= 0) spinnerBranch.setSelection(spinnerPosition);
        } else {
            Toast.makeText(this, "Customer info not found", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    private String getBranchNameById(int branchId) {
        switch (branchId) {
            case 1: return "Colombo";
            case 2: return "Kandy";
            default: return "Colombo";
        }
    }

    private void launchMenu(String orderType, int branchId, String address, LatLng latLng) {
        Intent intent = new Intent(OrderSetupActivity.this, menu.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userRole", "customer");
        intent.putExtra("branchId", branchId);
        intent.putExtra("orderType", orderType);
        intent.putExtra("branch", spinnerBranch.getSelectedItem().toString());
        intent.putExtra("defaultAddress", address);

        if (latLng != null) {
            intent.putExtra("lat", latLng.latitude);
            intent.putExtra("lng", latLng.longitude);
        }

        startActivity(intent);
    }

    private int getBranchIdByName(String branchName) {
        switch (branchName) {
            case "Colombo": return 1;
            case "Galle": return 2;
            default: return 1;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedAddress = data.getStringExtra("selected_address");
            double lat = data.getDoubleExtra("latitude", 0);
            double lng = data.getDoubleExtra("longitude", 0);
            selectedLatLng = new LatLng(lat, lng);
            tvDeliveryAddress.setText(selectedAddress);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) getCurrentLocation();
            else Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
