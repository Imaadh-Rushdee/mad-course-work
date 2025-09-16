package com.example.pizza_mania_app.customer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania_app.R;

public class orderTracking extends AppCompatActivity {

    private SQLiteDatabase db;

    private TextView tvOrderId, tvOrderCart, tvOrderAmount;
    private ImageView orderReadyImage, orderPickedUpImage, onTheWayImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_tracking);

        // Handle insets for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind UI elements
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderCart = findViewById(R.id.tvOrderCart);
        tvOrderAmount = findViewById(R.id.tvOrderAmount);

        orderReadyImage = findViewById(R.id.orderReadyImage);
        orderPickedUpImage = findViewById(R.id.orderPickedUpImage);
        onTheWayImage = findViewById(R.id.onTheWayImage);

        // Open or create database
        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        // Get orderId from Intent
        long orderId = getIntent().getLongExtra("orderId", -1);
        if (orderId != -1) {
            loadOrderTracking(orderId);
        }
    }

    private void loadOrderTracking(long orderId) {
        // Query the order data
        Cursor order = db.rawQuery(
                "SELECT order_status, order_cart, total FROM orders WHERE order_id=?",
                new String[]{String.valueOf(orderId)}
        );

        if (order.moveToFirst()) {
            String status = order.getString(0);  // order_status
            String cart = order.getString(1);    // order_cart
            double total = order.getDouble(2);   // total

            tvOrderId.setText("Order ID: " + orderId);
            tvOrderCart.setText("Items: " + cart);
            tvOrderAmount.setText("Amount: Rs. " + total);

            // Update status images based on order_status
            updateStatusUI(status);
        }
        order.close();
    }

    private void updateStatusUI(String status) {
        // Reset all images to grey
        orderReadyImage.setBackgroundResource(R.drawable.ic_circle_grey);
        orderPickedUpImage.setBackgroundResource(R.drawable.ic_circle_grey);
        onTheWayImage.setBackgroundResource(R.drawable.ic_circle_grey);

        switch (status.toLowerCase()) {
            case "ready":
                orderReadyImage.setBackgroundResource(R.drawable.ic_circle_green);
                break;
            case "on the way":
                orderReadyImage.setBackgroundResource(R.drawable.ic_circle_green);
                onTheWayImage.setBackgroundResource(R.drawable.ic_circle_green);
                break;
            case "picked up":
                orderReadyImage.setBackgroundResource(R.drawable.ic_circle_green);
                onTheWayImage.setBackgroundResource(R.drawable.ic_circle_green);
                orderPickedUpImage.setBackgroundResource(R.drawable.ic_circle_green);
                break;
            default:
                // Leave all grey for unknown status
                break;
        }
    }
}
