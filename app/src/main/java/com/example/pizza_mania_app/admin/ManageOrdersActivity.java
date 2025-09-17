package com.example.pizza_mania_app.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pizza_mania_app.R;
import com.example.pizza_mania_app.admin.Order;
import com.example.pizza_mania_app.admin.OrderAdapter;

import java.util.List;

public class ManageOrdersActivity extends AppCompatActivity {

    EditText etOrderName, etOrderStatus, etOrderId;
    Button btnAdd, btnUpdate, btnDelete, btnViewPending, btnViewCompleted, btnViewAll;
    RecyclerView recyclerOrders;

    com.example.pizza_mania_app.OrderDatabaseHelper dbHelper;
    OrderAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        etOrderName = findViewById(R.id.etOrderName);
        etOrderStatus = findViewById(R.id.etOrderStatus);
        etOrderId = findViewById(R.id.etOrderId);
        btnAdd = findViewById(R.id.btnAddOrder);
        btnUpdate = findViewById(R.id.btnUpdateOrder);
        btnDelete = findViewById(R.id.btnDeleteOrder);
        btnViewPending = findViewById(R.id.btnViewPending);
        btnViewCompleted = findViewById(R.id.btnViewCompleted);
        btnViewAll = findViewById(R.id.btnViewAll);
        recyclerOrders = findViewById(R.id.recyclerOrders);

        dbHelper = new com.example.pizza_mania_app.OrderDatabaseHelper(this);

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(dbHelper.getAllOrders());
        recyclerOrders.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            String name = etOrderName.getText().toString();
            String status = etOrderStatus.getText().toString();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(status)) {
                Toast.makeText(this, "Enter Name and Status", Toast.LENGTH_SHORT).show();
                return;
            }
            long id = dbHelper.addOrder(name, status);
            Toast.makeText(this, "Order Added (ID: " + id + ")", Toast.LENGTH_SHORT).show();
            refreshAll();
        });

        btnUpdate.setOnClickListener(v -> {
            String idStr = etOrderId.getText().toString();
            String name = etOrderName.getText().toString();
            String status = etOrderStatus.getText().toString();
            if (TextUtils.isEmpty(idStr) || TextUtils.isEmpty(name) || TextUtils.isEmpty(status)) {
                Toast.makeText(this, "Enter ID, Name, Status", Toast.LENGTH_SHORT).show();
                return;
            }
            int updated = dbHelper.updateOrder(Long.parseLong(idStr), name, status);
            if (updated > 0) {
                Toast.makeText(this, "Order Updated", Toast.LENGTH_SHORT).show();
                refreshAll();
            } else {
                Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            String idStr = etOrderId.getText().toString();
            if (TextUtils.isEmpty(idStr)) {
                Toast.makeText(this, "Enter Order ID", Toast.LENGTH_SHORT).show();
                return;
            }
            int deleted = dbHelper.deleteOrder(Long.parseLong(idStr));
            if (deleted > 0) {
                Toast.makeText(this, "Order Deleted", Toast.LENGTH_SHORT).show();
                refreshAll();
            } else {
                Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            }
        });

        btnViewPending.setOnClickListener(v -> {
            List<Order> pending = dbHelper.getPendingOrders();
            adapter.updateList(pending);
        });

        btnViewCompleted.setOnClickListener(v -> {
            List<Order> completed = dbHelper.getCompletedOrders();
            adapter.updateList(completed);
        });

        btnViewAll.setOnClickListener(v -> refreshAll());
    }

    private void refreshAll() {
        adapter.updateList(dbHelper.getAllOrders());
    }
}
