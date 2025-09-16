package com.example.pizza_mania_app.customer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania_app.R;

public class orderTracking extends AppCompatActivity {

    private SQLiteDatabase db;
    private TextView tvOrderId, tvOrderStatus, tvOrderDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_tracking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderDetails = findViewById(R.id.tvOrderDetails);

        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        long orderId = getIntent().getLongExtra("orderId", -1);
        if (orderId != -1) loadOrderTracking(orderId);
    }

    private void loadOrderTracking(long orderId) {
        Cursor order = db.rawQuery("SELECT order_status, order_cart, total FROM orders WHERE order_id=?", new String[]{String.valueOf(orderId)});
        if (order.moveToFirst()) {
            tvOrderId.setText("Order ID: " + orderId);
            tvOrderStatus.setText("Status: " + order.getString(0));
            tvOrderDetails.setText("Order Items: " + order.getString(1) + "\nTotal: Rs. " + order.getDouble(2));
        }
        order.close();
    }
}
