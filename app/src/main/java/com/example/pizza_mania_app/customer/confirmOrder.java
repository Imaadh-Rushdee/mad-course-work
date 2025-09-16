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

public class confirmOrder extends AppCompatActivity {

    private SQLiteDatabase db;
    private TextView tvAddress, tvTotalAmount;
    private Spinner spinnerBranch;
    private Button btnConfirmOrder, btnChangeAddress;
    private String userId;
    private double totalAmount = 0;

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

        userId = getIntent().getStringExtra("userId");
        tvAddress.setText(getIntent().getStringExtra("userAddress"));

        loadTotalAmount();

        btnChangeAddress.setOnClickListener(v -> tvAddress.setText("New Address, Colombo"));

        btnConfirmOrder.setOnClickListener(v -> {
            String address = tvAddress.getText().toString();
            String branch = spinnerBranch.getSelectedItem().toString();
            insertOrder(address, branch);
        });
    }

    private void loadTotalAmount() {
        Cursor cartCursor = db.rawQuery("SELECT ci.quantity, m.price FROM carts c JOIN cart_items ci ON c.cart_id=ci.cart_id JOIN menu_items m ON ci.item_id=m.item_id WHERE c.user_id=?", new String[]{userId});
        if (cartCursor.moveToFirst()) {
            do {
                totalAmount += cartCursor.getInt(0) * cartCursor.getDouble(1);
            } while (cartCursor.moveToNext());
        }
        cartCursor.close();
        tvTotalAmount.setText("Rs. " + totalAmount);
    }

    private void insertOrder(String address, String branch) {
        ContentValues orderValues = new ContentValues();
        orderValues.put("customer_name", userId);
        orderValues.put("order_address", address);
        orderValues.put("total", totalAmount);
        orderValues.put("order_status", "pending");
        orderValues.put("order_type", "delivery");

        long orderId = db.insert("orders", null, orderValues);

        if (orderId != -1) {
            Cursor items = db.rawQuery("SELECT ci.item_id, ci.quantity, m.price FROM carts c JOIN cart_items ci ON c.cart_id=ci.cart_id JOIN menu_items m ON ci.item_id=m.item_id WHERE c.user_id=?", new String[]{userId});
            if (items.moveToFirst()) {
                do {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put("order_id", orderId);
                    itemValues.put("item_id", items.getInt(0));
                    itemValues.put("quantity", items.getInt(1));
                    itemValues.put("price", items.getDouble(2));
                    db.insert("order_items", null, itemValues);
                } while (items.moveToNext());
            }
            items.close();

            db.execSQL("DELETE FROM cart_items WHERE cart_id IN (SELECT cart_id FROM carts WHERE user_id=?)", new String[]{userId});

            Toast.makeText(this, "Order Confirmed!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, orderInvoice.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to confirm order.", Toast.LENGTH_SHORT).show();
        }
    }
}
