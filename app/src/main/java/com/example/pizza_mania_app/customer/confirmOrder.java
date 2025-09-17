package com.example.pizza_mania_app.customer;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;
import com.example.pizza_mania_app.helperClasses.GoogleMapsHelper;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class confirmOrder extends AppCompatActivity {

    private SQLiteDatabase db;
    private TextView tvAddress, tvTotalAmount;
    private Spinner spinnerBranch;
    private Button btnConfirmOrder;
    private String userId;
    private String customerName;
    private String orderType;
    private double totalAmount = 0;

    private ArrayList<String> branchNames = new ArrayList<>();
    private ArrayList<Integer> branchIds = new ArrayList<>();
    private int selectedBranchId = -1;

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

        userId = getIntent().getStringExtra("userId");
        orderType = getIntent().getStringExtra("orderType");
        String userAddress = getIntent().getStringExtra("userAddress");

        if (userId == null || orderType == null || userAddress == null) {
            Toast.makeText(this, "Missing order information!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvAddress.setText(userAddress);

        // Geocode the address to get latitude/longitude
        LatLng latLng = GoogleMapsHelper.geocodeAddress(this, userAddress);
        if (latLng != null) {
            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;
        }

        // Load user's branch from database
        Cursor userCursor = db.rawQuery("SELECT branch_id FROM users WHERE user_id=?", new String[]{userId});
        if (userCursor.moveToFirst()) {
            selectedBranchId = userCursor.getInt(0);
        } else {
            Toast.makeText(this, "User branch not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userCursor.close();

        loadTotalAmount();
        loadBranches();

        btnConfirmOrder.setOnClickListener(v -> {
            String finalAddress = tvAddress.getText().toString().trim();
            insertOrder(finalAddress, selectedBranchId);
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

    private void loadBranches() {
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

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, branchNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBranch.setAdapter(adapter);

        // Preselect user's branch
        int index = branchIds.indexOf(selectedBranchId);
        if (index >= 0) spinnerBranch.setSelection(index);
    }

    private void insertOrder(String address, int branchId) {
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

        String orderDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String orderTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        ContentValues orderValues = new ContentValues();
        orderValues.put("customer_name", customerName);
        orderValues.put("order_cart", cartSummary.toString());
        orderValues.put("order_address", address);
        orderValues.put("address_latitude", selectedLat);
        orderValues.put("address_longitude", selectedLng);
        orderValues.put("total", calcTotal);
        orderValues.put("order_status", "ready");
        orderValues.put("order_type", orderType);
        orderValues.put("branch_id", branchId);
        orderValues.put("payment_method", "Cash");
        orderValues.put("order_date", orderDate);
        orderValues.put("order_time", orderTime);

        long orderId = db.insert("orders", null, orderValues);

        if (orderId != -1) {
            db.execSQL("DELETE FROM cart_items WHERE cart_id IN (SELECT cart_id FROM carts WHERE user_id=?)",
                    new String[]{userId});
            Toast.makeText(this, "Order Confirmed!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, orderTracking.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to confirm order.", Toast.LENGTH_SHORT).show();
        }
    }
}
