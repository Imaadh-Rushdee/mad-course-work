package com.example.pizza_mania_app.deliveryPartner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class deliveryPartnerDashboard extends AppCompatActivity {

    private LinearLayout ordersContainer; // Layout where order cards will be added
    private TextView tvNoOrders;          // Message shown when no orders are found
    private ImageButton refreshIcon;      // Refresh button to reload orders
    private TextView partnerNameText;     // To display partner name

    private FirebaseFirestore db;         // Firestore reference

    // Example partner details (In real app: get these from login)
    private String partnerBranchId = "branch_001";
    private String partnerName = "John Doe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_partner_dashboard);

        // Initialize views
        ordersContainer = findViewById(R.id.ordersContainer);
        tvNoOrders = findViewById(R.id.tvNoOrders);
        refreshIcon = findViewById(R.id.refreshIcon);
        partnerNameText = findViewById(R.id.driver_name);

        // Set welcome message
        partnerNameText.setText("Welcome " + partnerName + "!");

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load orders from Firestore
        loadOrdersFromFirestore();

        // Listen for real-time changes in Firestore
        listenOrdersRealtime();

        // Refresh button click → reload orders
        refreshIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing orders...", Toast.LENGTH_SHORT).show();
            ordersContainer.removeAllViews(); // Clear existing orders
            loadOrdersFromFirestore();       // Reload
        });
    }

    /**
     * Add a single order card to the UI
     */
    private void addOrderCard(String orderId, String customerName, String address, String totalPrice, String status) {
        // Inflate order_card.xml layout
        View card = getLayoutInflater().inflate(R.layout.order_card, ordersContainer, false);

        // Get views inside the card
        TextView tvOrderId = card.findViewById(R.id.tvOrderId);
        TextView tvCustomerName = card.findViewById(R.id.tvCustomerName);
        TextView tvOrderStatus = card.findViewById(R.id.tvOrderStatus);

        // Set data
        tvOrderId.setText("Order ID: " + orderId);
        tvCustomerName.setText("Customer: " + customerName);
        tvOrderStatus.setText("Status: " + status);

        // When user clicks the card → go to orderDetails page
        card.setOnClickListener(v -> {
            Intent intent = new Intent(deliveryPartnerDashboard.this, orderDetails.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("customerName", customerName);
            intent.putExtra("address", address);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("status", status);
            startActivity(intent);
        });

        // Add the card to the container
        ordersContainer.addView(card);
        tvNoOrders.setVisibility(View.GONE); // Hide "No orders" message
    }

    /**
     * Load all pending delivery orders from Firestore
     */
    private void loadOrdersFromFirestore() {
        db.collection("orders")
                .whereEqualTo("branchId", partnerBranchId)      // Orders for this branch only
                .whereEqualTo("status", "pending")             // Only pending orders
                .whereEqualTo("deliveryType", "delivery")      // Only delivery orders
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        tvNoOrders.setVisibility(View.VISIBLE); // Show "No orders" message
                        return;
                    }
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        addOrderCard(
                                doc.getId(),
                                doc.getString("customerName"),
                                doc.getString("delivery_address"),
                                doc.getDouble("total").toString(),
                                doc.getString("status")
                        );
                    }
                });
    }

    /**
     * Real-time updates for new or updated orders
     */
    private void listenOrdersRealtime() {
        db.collection("orders")
                .whereEqualTo("branchId", partnerBranchId)
                .whereEqualTo("status", "pending")
                .whereEqualTo("deliveryType", "delivery")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    // Clear and reload the list when changes happen
                    ordersContainer.removeAllViews();
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        DocumentSnapshot doc = dc.getDocument();
                        addOrderCard(
                                doc.getId(),
                                doc.getString("customerName"),
                                doc.getString("delivery_address"),
                                doc.getDouble("total").toString(),
                                doc.getString("status")
                        );
                    }
                });
    }
}
