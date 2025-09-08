package com.example.pizza_mania_app.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;

public class ManageCustomersActivity extends AppCompatActivity {

    EditText etCustomerName, etCustomerPhone;
    Button btnAddCustomer, btnUpdateCustomer, btnDeleteCustomer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_customers);

        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerPhone = findViewById(R.id.etCustomerPhone);
        btnAddCustomer = findViewById(R.id.btnAddCustomer);
        btnUpdateCustomer = findViewById(R.id.btnUpdateCustomer);
        btnDeleteCustomer = findViewById(R.id.btnDeleteCustomer);

        btnAddCustomer.setOnClickListener(v ->
                Toast.makeText(this, "Customer Added: " + etCustomerName.getText(), Toast.LENGTH_SHORT).show());

        btnUpdateCustomer.setOnClickListener(v ->
                Toast.makeText(this, "Customer Updated", Toast.LENGTH_SHORT).show());

        btnDeleteCustomer.setOnClickListener(v ->
                Toast.makeText(this, "Customer Deleted", Toast.LENGTH_SHORT).show());
    }
}
