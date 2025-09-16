package com.example.pizza_mania_app.customer;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
        String address = getIntent().getStringExtra("userAddress");
        int branchIdFromMenu = getIntent().getIntExtra("branchId", -1); // from menu

        if (userId == null || address == null || orderType == null) {
            Toast.makeText(this, "Missing order information!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvAddress.setText(address);

        loadTotalAmount();
        loadBranches(branchIdFromMenu);

        // Change address button (replace with map picker later if needed)
        btnChangeAddress.setOnClickListener(v -> tvAddress.setText("New Address, Colombo"));

        // Confirm order button
        btnConfirmOrder.setOnClickListener(v -> {
            String finalAddress = tvAddress.getText() != null ? tvAddress.getText().toString() : "";

            int position = spinnerBranch.getSelectedItemPosition();
            if (position < 0 || position >= branchIds.size()) {
                Toast.makeText(this, "Please select a branch!", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedBranchId = branchIds.get(position);

            insertOrder(finalAddress, selectedBranchId);
        });
    }

    /** Load total amount from cart **/
    private void loadTotalAmount() {
        totalAmount = 0;
        Cursor cartCursor = db.rawQuery(
                "SELECT ci.quantity, m.price " +
                        "FROM carts c " +
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

    /** Load branch list into spinner **/
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

    /** Insert order into DB **/
    private void insertOrder(String address, int branchId) {
        // 1. Get customer name
        String customerName = "";
        Cursor userCursor = db.rawQuery("SELECT name FROM users WHERE user_id=?", new String[]{userId});
        if (userCursor.moveToFirst()) {
            customerName = userCursor.getString(0);
        }
        userCursor.close();

        // 2. Build cart summary
        StringBuilder cartSummary = new StringBuilder();
        totalAmount = 0;

        // FIXED: use 'name' column from menu_items instead of 'item_name'
        Cursor items = db.rawQuery(
                "SELECT m.name, ci.quantity, m.price " +
                        "FROM carts c " +
                        "JOIN cart_items ci ON c.cart_id = ci.cart_id " +
                        "JOIN menu_items m ON ci.item_id = m.item_id " +
                        "WHERE c.user_id=?",
                new String[]{userId});

        if (items.moveToFirst()) {
            do {
                String itemName = items.getString(0);
                int quantity = items.getInt(1);
                double price = items.getDouble(2);

                double itemTotal = price * quantity;
                totalAmount += itemTotal;

                cartSummary.append(itemName)
                        .append(" x ").append(quantity)
                        .append(" (Rs.").append(itemTotal).append(")\n");
            } while (items.moveToNext());
        }
        items.close();

        // 3. Insert into orders
        ContentValues orderValues = new ContentValues();
        orderValues.put("customer_name", customerName);
        orderValues.put("order_cart", cartSummary.toString());
        orderValues.put("order_address", address);
        orderValues.put("total", totalAmount);
        orderValues.put("order_status", "pending");
        orderValues.put("order_type", orderType);
        orderValues.put("branch_id", branchId);
        orderValues.put("order_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        orderValues.put("order_time", new SimpleDateFormat("HH:mm:ss").format(new Date()));

        long orderId = db.insert("orders", null, orderValues);

        // 4. If success, clear cart
        if (orderId != -1) {
            db.execSQL("DELETE FROM cart_items WHERE cart_id IN (SELECT cart_id FROM carts WHERE user_id=?)", new String[]{userId});
            Toast.makeText(this, "Order Confirmed!", Toast.LENGTH_SHORT).show();

            // 5. Go to tracking page
            Intent intent = new Intent(this, orderTracking.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to confirm order.", Toast.LENGTH_SHORT).show();
        }
    }
}
