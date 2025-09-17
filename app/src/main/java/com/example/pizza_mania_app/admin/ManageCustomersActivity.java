package com.example.pizza_mania_app.admin;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;

import java.util.ArrayList;

public class ManageCustomersActivity extends AppCompatActivity {

    EditText etCustomerId, etCustomerName, etCustomerPhone, etSearchName;
    Button btnAddCustomer, btnUpdateCustomer, btnDeleteCustomer, btnSearchCustomer;
    ListView lvCustomers;
    com.example.pizza_mania_app.admin.DatabaseHelper dbHelper;
    ArrayList<String> customerList;
    ArrayAdapter<String> adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_customers);

        etCustomerId = findViewById(R.id.etCustomerId);
        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerPhone = findViewById(R.id.etCustomerPhone);
        etSearchName = findViewById(R.id.etSearchName);

        btnAddCustomer = findViewById(R.id.btnAddCustomer);
        btnUpdateCustomer = findViewById(R.id.btnUpdateCustomer);
        btnDeleteCustomer = findViewById(R.id.btnDeleteCustomer);
        btnSearchCustomer = findViewById(R.id.btnSearchCustomer);

        lvCustomers = findViewById(R.id.lvCustomers);

        dbHelper = new com.example.pizza_mania_app.admin.DatabaseHelper(this);
        customerList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, customerList);
        lvCustomers.setAdapter(adapter);

        loadCustomerList(); // Load all customers into ListView

        // Clicking on a customer will fill ID, Name, Phone
        lvCustomers.setOnItemClickListener((parent, view, position, id) -> {
            String selected = customerList.get(position);
            // Format: "ID:1 Name:John Phone:123456789"
            String[] parts = selected.split(" ");
            etCustomerId.setText(parts[0].split(":")[1]);
            etCustomerName.setText(parts[1].split(":")[1]);
            etCustomerPhone.setText(parts[2].split(":")[1]);
        });

        // ADD CUSTOMER
        btnAddCustomer.setOnClickListener(v -> {
            String name = etCustomerName.getText().toString().trim();
            String phone = etCustomerPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Enter name and phone", Toast.LENGTH_SHORT).show();
                return;
            }

            long id = dbHelper.addCustomer(name, phone);
            if (id != -1) {
                Toast.makeText(this, "Customer Added. ID: " + id, Toast.LENGTH_LONG).show();
                etCustomerId.setText(String.valueOf(id));
                etCustomerName.setText("");
                etCustomerPhone.setText("");
                loadCustomerList();
            } else {
                Toast.makeText(this, "Error adding customer", Toast.LENGTH_SHORT).show();
            }
        });

        // UPDATE CUSTOMER
        btnUpdateCustomer.setOnClickListener(v -> {
            String idStr = etCustomerId.getText().toString().trim();
            String name = etCustomerName.getText().toString().trim();
            String phone = etCustomerPhone.getText().toString().trim();

            if (idStr.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Enter ID, name and phone", Toast.LENGTH_SHORT).show();
                return;
            }

            int rows = dbHelper.updateCustomer(Long.parseLong(idStr), name, phone);
            if (rows > 0) {
                Toast.makeText(this, "Customer Updated", Toast.LENGTH_SHORT).show();
                loadCustomerList();
            } else {
                Toast.makeText(this, "Update failed. Customer not found", Toast.LENGTH_SHORT).show();
            }
        });

        // DELETE CUSTOMER
        btnDeleteCustomer.setOnClickListener(v -> {
            String idStr = etCustomerId.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Enter Customer ID", Toast.LENGTH_SHORT).show();
                return;
            }

            int rows = dbHelper.deleteCustomer(Long.parseLong(idStr));
            if (rows > 0) {
                Toast.makeText(this, "Customer Deleted", Toast.LENGTH_SHORT).show();
                etCustomerId.setText("");
                etCustomerName.setText("");
                etCustomerPhone.setText("");
                loadCustomerList();
            } else {
                Toast.makeText(this, "Delete failed. Customer not found", Toast.LENGTH_SHORT).show();
            }
        });

        // SEARCH CUSTOMER BY NAME
        btnSearchCustomer.setOnClickListener(v -> {
            String name = etSearchName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter name to search", Toast.LENGTH_SHORT).show();
                return;
            }
            loadCustomerList(name);
        });
    }

    // Load all customers
    private void loadCustomerList() {
        customerList.clear();
        Cursor cursor = dbHelper.getAllCustomers();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_PHONE));
                customerList.add("ID:" + id + " Name:" + name + " Phone:" + phone);
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    // Load customers by search
    private void loadCustomerList(String nameSearch) {
        customerList.clear();
        Cursor cursor = dbHelper.searchCustomersByName(nameSearch);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_PHONE));
                customerList.add("ID:" + id + " Name:" + name + " Phone:" + phone);
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }
}
