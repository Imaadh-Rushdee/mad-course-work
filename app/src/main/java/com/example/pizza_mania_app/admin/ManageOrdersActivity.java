package com.example.pizza_mania_app.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;

public class ManageOrdersActivity extends AppCompatActivity {

    EditText etOrderId, etOrderStatus;
    Button btnAddOrder, btnUpdateOrder, btnDeleteOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        etOrderId = findViewById(R.id.etOrderId);
        etOrderStatus = findViewById(R.id.etOrderStatus);
        btnAddOrder = findViewById(R.id.btnAddOrder);
        btnUpdateOrder = findViewById(R.id.btnUpdateOrder);
        btnDeleteOrder = findViewById(R.id.btnDeleteOrder);

        btnAddOrder.setOnClickListener(v ->
                Toast.makeText(this, "Order Added: " + etOrderId.getText(), Toast.LENGTH_SHORT).show());

        btnUpdateOrder.setOnClickListener(v ->
                Toast.makeText(this, "Order Updated", Toast.LENGTH_SHORT).show());

        btnDeleteOrder.setOnClickListener(v ->
                Toast.makeText(this, "Order Deleted", Toast.LENGTH_SHORT).show());
    }
}
