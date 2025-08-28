package com.example.pizza_mania_app.deliveryPartner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania_app.R;

public class deliveryPartnerDashboard extends AppCompatActivity {

    private LinearLayout ordersContainer;
    private TextView tvNoOrders;
    private ImageButton refreshIcon;
    private TextView partnerNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_partner_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ordersContainer = findViewById(R.id.ordersContainer);
        tvNoOrders = findViewById(R.id.tvNoOrders);
        refreshIcon = findViewById(R.id.refreshIcon);
        partnerNameText = findViewById(R.id.driver_name);

        partnerNameText.setText("Welcome Partner!");

        // Show a dummy order card for now
        showDummyOrder();

        // Refresh button listener (just shows toast for now)
        refreshIcon.setOnClickListener(v -> Toast.makeText(this, "Refreshing orders...", Toast.LENGTH_SHORT).show());
    }

    private void showDummyOrder() {
        // Hide "No Orders" text
        tvNoOrders.setVisibility(View.GONE);

        // Show scroll container
        findViewById(R.id.scrollOrders).setVisibility(View.VISIBLE);

        // Inflate dummy card
        View card = getLayoutInflater().inflate(R.layout.order_card, ordersContainer, false);

        // Set dummy values
        TextView tvOrderId = card.findViewById(R.id.tvOrderId);
        TextView tvCustomerName = card.findViewById(R.id.tvCustomerName);
        TextView tvOrderStatus = card.findViewById(R.id.tvOrderStatus);

        tvOrderId.setText("Order ID: 123");
        tvCustomerName.setText("Customer: John Doe");
        tvOrderStatus.setText("Status: Pending");

        // Click listener to open order details
        card.setOnClickListener(v -> {
            Intent intent = new Intent(deliveryPartnerDashboard.this, orderDetails.class);

            // Pass dummy data
            intent.putExtra("orderId", "123");
            intent.putExtra("customerName", "John Doe");
            intent.putExtra("address", "123 Main Street, Colombo");
            intent.putExtra("totalPrice", "2500");

            startActivity(intent);
        });

        // Add card to container
        ordersContainer.addView(card);
    }


    // You can reuse this later for backend data
    private void addOrderCard(String orderId, String customerName, String address, String totalPrice) {
        // Inflate the XML layout
        View card = getLayoutInflater().inflate(R.layout.order_card, ordersContainer, false);

        // Find the TextViews inside the card
        TextView tvOrderId = card.findViewById(R.id.tvOrderId);
        TextView tvCustomerName = card.findViewById(R.id.tvCustomerName);
        TextView tvOrderStatus = card.findViewById(R.id.tvOrderStatus);

        // Set the text dynamically
        tvOrderId.setText("Order ID: " + orderId);
        tvCustomerName.setText("Customer: " + customerName);
        tvOrderStatus.setText("Status: Pending");

        // Add click listener to open order details
        card.setOnClickListener(v -> {
            Intent intent = new Intent(deliveryPartnerDashboard.this, orderDetails.class);

            // Pass dummy data (replace with real DB values later)
            intent.putExtra("orderId", orderId);
            intent.putExtra("customerName", customerName);
            intent.putExtra("address", address);
            intent.putExtra("totalPrice", totalPrice);

            startActivity(intent);
        });

        // Add the card to the container
        ordersContainer.addView(card);
    }

}
