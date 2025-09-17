package com.example.pizza_mania_app.admin;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.OrderDatabaseHelper;
import com.example.pizza_mania_app.R;

import java.util.List;

public class ManageOrdersActivity extends AppCompatActivity {

    private OrderDatabaseHelper dbHelper;
    private LinearLayout llOrdersContainer;
    private int branchId; // admin's branch

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        llOrdersContainer = findViewById(R.id.llOrdersContainer);
        dbHelper = new OrderDatabaseHelper(this);

        // Get branchId from intent or default
        branchId = getIntent().getIntExtra("branch_id", -1);

        showPendingOrders(branchId);
    }

    private void showPendingOrders(int branchId) {
        llOrdersContainer.removeAllViews();

        List<Order> pendingOrders = dbHelper.getPendingOrdersByBranch(branchId);

        if (pendingOrders.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No pending orders");
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 50, 0, 0);
            llOrdersContainer.addView(empty);
            return;
        }

        for (Order order : pendingOrders) {
            LinearLayout orderLayout = new LinearLayout(this);
            orderLayout.setOrientation(LinearLayout.VERTICAL);
            orderLayout.setPadding(24, 24, 24, 24);
            orderLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

            TextView tvOrder = new TextView(this);
            tvOrder.setText("Order ID: " + order.getId() +
                    "\nName: " + order.getName() +
                    "\nStatus: " + order.getStatus());
            tvOrder.setPadding(0, 0, 0, 16);
            orderLayout.addView(tvOrder);

            LinearLayout btnLayout = new LinearLayout(this);
            btnLayout.setOrientation(LinearLayout.HORIZONTAL);

            Button btnReady = new Button(this);
            btnReady.setText("Order Ready");
            btnReady.setOnClickListener(v -> {
                dbHelper.updateOrderStatus(order.getId(), "completed");
                Toast.makeText(this, "Order marked as ready", Toast.LENGTH_SHORT).show();
                showPendingOrders(branchId);
            });

            Button btnReject = new Button(this);
            btnReject.setText("Reject");
            btnReject.setOnClickListener(v -> {
                dbHelper.deleteOrder(order.getId());
                Toast.makeText(this, "Order rejected", Toast.LENGTH_SHORT).show();
                showPendingOrders(branchId);
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            );
            params.setMargins(8, 0, 8, 0);
            btnReady.setLayoutParams(params);
            btnReject.setLayoutParams(params);

            btnLayout.addView(btnReady);
            btnLayout.addView(btnReject);
            orderLayout.addView(btnLayout);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, 0, 24);
            orderLayout.setLayoutParams(layoutParams);

            llOrdersContainer.addView(orderLayout);
        }
    }
}
