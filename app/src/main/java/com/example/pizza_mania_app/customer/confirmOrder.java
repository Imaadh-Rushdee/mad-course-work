package com.example.pizza_mania_app.customer;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;
import com.example.pizza_mania_app.helperClasses.SelectLocationActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class confirmOrder extends AppCompatActivity {

    private SQLiteDatabase db;
    private TextView tvAddress, tvTotalAmount;
    private Spinner spinnerBranch;
    private Button btnConfirmOrder, btnChangeAddress;
    private String userId;
    private String orderType;
    private double totalAmount = 0;

    private ArrayList<String> branchNames = new ArrayList<>();
    private ArrayList<Integer> branchIds = new ArrayList<>();
    private int selectedBranchId = -1;

    private static final int REQUEST_SELECT_LOCATION = 1001;
    // these will be set either by map picker or by geocoding the address
    private double selectedLat = 0.0, selectedLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        tvAddress = findViewById(R.id.tvAddress);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        spinnerBranch = findViewById(R.id.spinnerBranch);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);

        // Get intent extras
        userId = getIntent().getStringExtra("userId");
        orderType = getIntent().getStringExtra("orderType");
        String address = getIntent().getStringExtra("userAddress"); // initial address text
        int branchIdFromMenu = getIntent().getIntExtra("branchId", -1); // from menu

        if (userId == null || address == null || orderType == null) {
            Toast.makeText(this, "Missing order information!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvAddress.setText(address);

        // load totals & branches
        loadTotalAmount();
        loadBranches(branchIdFromMenu);

        // open map to change address (returns selected_address, latitude, longitude)
        btnChangeAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectLocationActivity.class);
            startActivityForResult(intent, REQUEST_SELECT_LOCATION);
        });

        btnConfirmOrder.setOnClickListener(v -> {
            String finalAddress = tvAddress.getText() != null ? tvAddress.getText().toString().trim() : "";

            // For Pickup → branch is mandatory
            if ("Pickup".equalsIgnoreCase(orderType)) {
                int position = spinnerBranch.getSelectedItemPosition();
                if (position < 0 || position >= branchIds.size()) {
                    Toast.makeText(this, "Please select a branch!", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedBranchId = branchIds.get(position);
            }

            // Ensure lat/lng correspond to finalAddress:
            double[] latLng = resolveLatLngForAddress(finalAddress);
            double latitude = latLng[0];
            double longitude = latLng[1];

            // Insert order with lat/lng
            insertOrder(finalAddress, selectedBranchId, latitude, longitude);
        });
    }

    private void loadTotalAmount() {
        totalAmount = 0;
        Cursor cartCursor = db.rawQuery(
                "SELECT ci.quantity, m.price FROM carts c " +
                        "JOIN cart_items ci ON c.cart_id=ci.cart_id " +
                        "JOIN menu_items m ON ci.item_id=m.item_id " +
                        "WHERE c.user_id=?",
                new String[]{userId});

        if (cartCursor.moveToFirst()) {
            do {
                totalAmount += cartCursor.getInt(0) * cartCursor.getDouble(1);
            } while (cartCursor.moveToNext());
        }
        cartCursor.close();

        tvTotalAmount.setText("Rs. " + totalAmount);
    }

    private void loadBranches(int branchIdFromMenu) {
        branchNames.clear();
        branchIds.clear();

        Cursor cursor = db.rawQuery("SELECT branch_id, branch_name FROM branches", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("branch_id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("branch_name"));
            branchIds.add(id);
            branchNames.add(name);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branchNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBranch.setAdapter(adapter);

        // Preselect the branch passed from menu
        if (branchIdFromMenu > 0) {
            int index = branchIds.indexOf(branchIdFromMenu);
            if (index >= 0) spinnerBranch.setSelection(index);
        }
    }

    /**
     * Resolve lat/lng for the given address. Order of attempts:
     *  1) If user already picked location via map picker, use selectedLat/selectedLng
     *  2) Try Geocoder on finalAddress
     *  3) Try Geocoder on user's saved address in users table
     *  4) If all fail return {0.0, 0.0} and show a toast warning
     */
    private double[] resolveLatLngForAddress(String finalAddress) {
        double lat = selectedLat;
        double lng = selectedLng;

        // 1) If map picker already provided coordinates, use them
        if (lat != 0.0 || lng != 0.0) {
            return new double[]{lat, lng};
        }

        // Helper: attempt geocode for a given address string
        java.util.function.Function<String, double[]> tryGeocode = (addr) -> {
            if (addr == null || addr.trim().isEmpty()) return new double[]{0.0, 0.0};
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(addr, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address a = addresses.get(0);
                    return new double[]{a.getLatitude(), a.getLongitude()};
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new double[]{0.0, 0.0};
        };

        // 2) Try finalAddress
        double[] res = tryGeocode.apply(finalAddress);
        if (res[0] != 0.0 || res[1] != 0.0) return res;

        // 3) Try user's saved address from users table
        Cursor c = db.rawQuery("SELECT address FROM users WHERE user_id=?", new String[]{userId});
        if (c.moveToFirst()) {
            String userAddr = c.getString(0);
            if (userAddr != null && !userAddr.trim().isEmpty()) {
                res = tryGeocode.apply(userAddr);
                if (res[0] != 0.0 || res[1] != 0.0) {
                    c.close();
                    return res;
                }
            }
        }
        c.close();

        // 4) failed to resolve
        Toast.makeText(this, "Warning: couldn't resolve coordinates for address. Saved as 0,0", Toast.LENGTH_SHORT).show();
        return new double[]{0.0, 0.0};
    }

    private void insertOrder(String address, int branchId, double latitude, double longitude) {
        // Build cart summary and recalc total
        StringBuilder cartSummary = new StringBuilder();
        double calcTotal = 0;

        Cursor items = db.rawQuery(
                "SELECT m.name, ci.quantity, m.price FROM carts c " +
                        "JOIN cart_items ci ON c.cart_id=ci.cart_id " +
                        "JOIN menu_items m ON ci.item_id=m.item_id " +
                        "WHERE c.user_id=?",
                new String[]{userId});

        if (items.moveToFirst()) {
            do {
                String itemName = items.getString(0);
                int qty = items.getInt(1);
                double price = items.getDouble(2);
                double itemTotal = price * qty;
                calcTotal += itemTotal;

                cartSummary.append(itemName).append(" x ").append(qty)
                        .append(" (Rs.").append(itemTotal).append(")\n");
            } while (items.moveToNext());
        }
        items.close();

        // Use calcTotal (consistent)
        ContentValues orderValues = new ContentValues();
        // store customer name or id as you prefer; here store user_id as text
        orderValues.put("customer_name", userId);
        orderValues.put("order_cart", cartSummary.toString());
        orderValues.put("order_address", address);
        orderValues.put("address_latitude", latitude);
        orderValues.put("address_longitude", longitude);
        orderValues.put("total", calcTotal);
        orderValues.put("order_status", "pending");
        orderValues.put("order_type", orderType);
        orderValues.put("branch_id", branchId);

        long orderId = db.insert("orders", null, orderValues);

        if (orderId != -1) {
            // clear cart items for user
            db.execSQL("DELETE FROM cart_items WHERE cart_id IN (SELECT cart_id FROM carts WHERE user_id=?)", new String[]{userId});

            Toast.makeText(this, "Order Confirmed!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, orderTracking.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to confirm order.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle result from map picker - map should return selected_address, latitude, longitude
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_LOCATION && resultCode == RESULT_OK && data != null) {
            String selectedAddress = data.getStringExtra("selected_address");
            selectedLat = data.getDoubleExtra("latitude", 0.0);
            selectedLng = data.getDoubleExtra("longitude", 0.0);

            if (selectedAddress != null) tvAddress.setText(selectedAddress);
        }
    }
}
