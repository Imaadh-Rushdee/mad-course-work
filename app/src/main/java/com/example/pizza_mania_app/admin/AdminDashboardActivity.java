package com.example.pizza_mania_app.admin;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;

public class AdminDashboardActivity extends AppCompatActivity {

    LinearLayout btnManageItems, btnManageCustomers, btnManageOrders, btnManageStaff, btnManageTransactions;
    Button btnAddQuickItem, btnViewReports;
    EditText etSearch;

    TextView tvItemCount, tvOrderCount, tvCustomerCount; // Stats overview

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Init Views
        btnManageItems = findViewById(R.id.btnManageItems);
        btnManageCustomers = findViewById(R.id.btnManageCustomers);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnManageStaff = findViewById(R.id.btnManageStaff);
        btnManageTransactions = findViewById(R.id.btnManageTransactions);

        btnAddQuickItem = findViewById(R.id.btnAddQuickItem);
        btnViewReports = findViewById(R.id.btnViewReports);
        etSearch = findViewById(R.id.etSearch);

        tvItemCount = findViewById(R.id.tvItemCount);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        tvCustomerCount = findViewById(R.id.tvCustomerCount);

        // --- Dashboard Navigation ---
        btnManageItems.setOnClickListener(v ->
                startActivity(new Intent(this, ManageItemsActivity.class)));

        btnManageCustomers.setOnClickListener(v ->
                startActivity(new Intent(this, ManageCustomersActivity.class)));

        btnManageOrders.setOnClickListener(v ->
                startActivity(new Intent(this, ManageOrdersActivity.class)));

        btnManageStaff.setOnClickListener(v ->
                startActivity(new Intent(this, ManageStaffActivity.class)));

        btnManageTransactions.setOnClickListener(v ->
                startActivity(new Intent(this, ManageTransactionsActivity.class)));

        // --- Quick Actions ---
        btnAddQuickItem.setOnClickListener(v ->
                startActivity(new Intent(this, ManageItemsActivity.class))); // Shortcut

        btnViewReports.setOnClickListener(v ->
                startActivity(new Intent(this, ReportsActivity.class)));

        // Example: set dummy stats
        tvItemCount.setText("42");
        tvOrderCount.setText("15");
        tvCustomerCount.setText("120");
    }
}
