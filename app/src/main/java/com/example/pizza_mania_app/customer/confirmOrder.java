package com.example.pizza_mania_app;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class confirmOrder extends AppCompatActivity {

    private SQLiteDatabase db;

    private TextView tvAddress, tvTotalAmount;
    private RadioGroup paymentMethodGroup;
    private RadioButton rbCash, rbCard;
    private Spinner spinnerBranch;
    private Button btnConfirmOrder, btnChangeAddress;

    // Example default order info (replace with actual cart data from previous activity)
    private String customerName = "John Cena";
    private String orderCart = "Sausage Pizza(2)";
    private double totalAmount = 5000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        // Open existing database
        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        tvAddress = findViewById(R.id.tvAddress);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        rbCash = findViewById(R.id.rbCash);
        rbCard = findViewById(R.id.rbCard);
        spinnerBranch = findViewById(R.id.spinnerBranch);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);

        // Set default values
        tvAddress.setText("Colombo, Mount Lavinia, No 63");
        tvTotalAmount.setText("Rs. " + totalAmount);


        // Change Address button
        btnChangeAddress.setOnClickListener(v -> {
            tvAddress.setText("New Address, Colombo");
        });

        // Confirm Order button
        btnConfirmOrder.setOnClickListener(v -> {
            String paymentMethod = rbCash.isChecked() ? "Cash" : "Card";
            String address = tvAddress.getText().toString();
            String branch = spinnerBranch.getSelectedItem().toString();

            insertOrder(customerName, orderCart, address, totalAmount, paymentMethod, branch);
        });
    }

    private void insertOrder(String customerName, String cart, String address, double total, String paymentMethod, String branch) {
        try {
            ContentValues orderValues = new ContentValues();
            orderValues.put("customer_name", customerName);
            orderValues.put("order_cart", cart);
            orderValues.put("order_address", address);
            orderValues.put("address_latitude", 0.0);  // Replace with actual lat if using map
            orderValues.put("address_longitude", 0.0);
            orderValues.put("payment_method", paymentMethod);
            orderValues.put("order_date", "2025-09-07");
            orderValues.put("order_time", "01:00 PM");
            orderValues.put("total", total);
            orderValues.put("order_status", "pending");
            orderValues.put("order_type", "delivery");

            long orderId = db.insert("orders", null, orderValues);
            if (orderId != -1) {
                Toast.makeText(this, "Order Confirmed! ✅", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(confirmOrder.this, menu.class)); // Go to menu or home
                finish();
            } else {
                Toast.makeText(this, "Failed to confirm order ❌", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "DB Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
