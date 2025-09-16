package com.example.pizza_mania_app.customer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania_app.R;

public class orderInvoice extends AppCompatActivity {

    private SQLiteDatabase db;
    private TextView tvOrderId, tvCustomerName, tvOrderCart, tvTotalAmount, tvOrderAddress;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_invoice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvOrderId = findViewById(R.id.tvOrderId);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvOrderCart = findViewById(R.id.tvOrderCart);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvOrderAddress = findViewById(R.id.tvOrderAddress);
        btnBack = findViewById(R.id.btnBackToMenu);

        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        long orderId = getIntent().getLongExtra("orderId", -1);
        if (orderId != -1) loadOrder(orderId);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadOrder(long orderId) {
        Cursor order = db.rawQuery("SELECT * FROM orders WHERE order_id=?", new String[]{String.valueOf(orderId)});
        if (order.moveToFirst()) {
            tvOrderId.setText("Order ID: " + order.getInt(order.getColumnIndexOrThrow("order_id")));
            tvCustomerName.setText("Customer: " + order.getString(order.getColumnIndexOrThrow("customer_name")));
            tvTotalAmount.setText("Total: Rs. " + order.getDouble(order.getColumnIndexOrThrow("total")));
            tvOrderAddress.setText("Address: " + order.getString(order.getColumnIndexOrThrow("order_address")));
        }
        order.close();

        StringBuilder cartItems
