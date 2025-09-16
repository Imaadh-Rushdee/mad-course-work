package com.example.pizza_mania_app.admin;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pizza_mania_app.R;
import com.example.pizza_mania_app.menu;

public class AdminDashboardActivity extends AppCompatActivity {

    Button btnOrders, btnMenu, btnUsers, btnTransactions;

    int userId, branchId; // coming from Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // --- Get userId and branchId from Intent ---
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        branchId = intent.getIntExtra("branchId", -1);

        // --- Init Views ---
        btnOrders = findViewById(R.id.btnOrders);
        btnMenu = findViewById(R.id.btnMenu);
        btnUsers = findViewById(R.id.btnUsers);
        btnTransactions = findViewById(R.id.btnTransactions);

        // --- Navigation with userId + branchId passed along ---
        btnOrders.setOnClickListener(v -> {
            Intent i = new Intent(this, ManageOrdersActivity.class);
            i.putExtra("userId", userId);
            i.putExtra("branchId", branchId);
            startActivity(i);
        });

        btnMenu.setOnClickListener(v -> {
            Intent i = new Intent(this, menu.class);
            i.putExtra("userId", userId);
            i.putExtra("userRole","admin");
            i.putExtra("branch","colombo");
            i.putExtra("branchId", branchId);
            startActivity(i);
        });

        btnUsers.setOnClickListener(v -> {
            Intent i = new Intent(this, ManageCustomersActivity.class);
            i.putExtra("userId", userId);
            i.putExtra("branchId", branchId);
            startActivity(i);
        });

        btnTransactions.setOnClickListener(v -> {
            Intent i = new Intent(this, ManageTransactionsActivity.class);
            i.putExtra("userId", userId);
            i.putExtra("branchId", branchId);
            startActivity(i);
        });
    }
}
