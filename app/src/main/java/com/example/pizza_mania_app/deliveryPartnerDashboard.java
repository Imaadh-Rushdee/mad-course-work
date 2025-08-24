package com.example.pizza_mania_app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class deliveryPartnerDashboard extends AppCompatActivity {

    private TextView riderName, riderEmail;
    private LinearLayout ordersContainer;

    private FirebaseFirestore db;
    private String partnerId = "partner_001"; // You can get this from login session

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

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        riderName = findViewById(R.id.welcomeText);
        riderEmail = findViewById(R.id.welcomeText);
        ordersContainer = findViewById(R.id.ordersRecyclerView);

        // Load data
        getDeliveryPartnerDetails();
        getDeliveryOrders();
    }

    private void getDeliveryPartnerDetails() {
        db.collection("delivery_partners").document(partnerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");

                        riderName.setText(name);
                        riderEmail.setText(email);
                    } else {
                        Toast.makeText(this, "Partner details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void getDeliveryOrders() {
        db.collection("orders")
                .whereEqualTo("partner_id", partnerId)
                .whereEqualTo("status", "assigned")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ordersContainer.removeAllViews(); // Clear old orders

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String orderId = doc.getId();
                        String customerName = doc.getString("customer_name");
                        String address = doc.getString("address");
                        String amount = doc.getString("amount");

                        // Create a simple card view dynamically
                        View orderCard = getLayoutInflater().inflate(R.layout.order_card, null);
                        TextView orderTitle = orderCard.findViewById(R.id.orderTitle);
                        TextView orderDetails = orderCard.findViewById(R.id.orderDetails);

                        orderTitle.setText("Order ID: " + orderId);
                        orderDetails.setText("Customer: " + customerName + "\nAddress: " + address + "\nAmount: $" + amount);

                        // Click event for the order card
                        orderCard.setOnClickListener(v -> onOrderClick(orderId));

                        ordersContainer.addView(orderCard);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void onOrderClick(String orderId) {
        Toast.makeText(this, "Clicked Order: " + orderId, Toast.LENGTH_SHORT).show();
        // Here you can start a new activity to show order details
    }

    public void refreshOrders(View view) {
        getDeliveryOrders();
    }
}
