package com.example.pizza_mania_app.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pizza_mania_app.R;
import android.database.Cursor;

import java.util.ArrayList;

public class ManageStaffActivity extends AppCompatActivity {

    EditText etStaffId, etStaffName, etStaffRole;
    Button btnAddStaff, btnUpdateStaff, btnDeleteStaff, btnViewStaff;
    ListView lvStaff;
    com.example.pizza_mania_app.admin.DatabaseHelper dbHelper;
    ArrayList<String> staffList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_staff);

        etStaffId = findViewById(R.id.etStaffId);
        etStaffName = findViewById(R.id.etStaffName);
        etStaffRole = findViewById(R.id.etStaffRole);
        btnAddStaff = findViewById(R.id.btnAddStaff);
        btnUpdateStaff = findViewById(R.id.btnUpdateStaff);
        btnDeleteStaff = findViewById(R.id.btnDeleteStaff);
        lvStaff = findViewById(R.id.lvStaff);
        btnViewStaff = findViewById(R.id.btnViewStaff);

        dbHelper = new com.example.pizza_mania_app.admin.DatabaseHelper(this);
        staffList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, staffList);
        lvStaff.setAdapter(adapter);

        loadStaffList();

        lvStaff.setOnItemClickListener((parent, view, position, id) -> {
            String selected = staffList.get(position);
            String[] parts = selected.split(" ");
            etStaffId.setText(parts[0].split(":")[1]);
            etStaffName.setText(parts[1].split(":")[1]);
            etStaffRole.setText(parts[2].split(":")[1]);
        });

        btnViewStaff.setOnClickListener(v -> {
            startActivity(new Intent(ManageStaffActivity.this, ViewStaffActivity.class));
        });

        btnAddStaff.setOnClickListener(v -> {
            String name = etStaffName.getText().toString().trim();
            String role = etStaffRole.getText().toString().trim();

            if (name.isEmpty() || role.isEmpty()) {
                Toast.makeText(this, "Enter staff name and role", Toast.LENGTH_SHORT).show();
                return;
            }

            long staffId = dbHelper.addStaff(name, role);

            if (staffId != -1) {
                Toast.makeText(this, "Staff Added Successfully. ID: " + staffId, Toast.LENGTH_LONG).show();
                etStaffId.setText(String.valueOf(staffId));
                etStaffName.setText("");
                etStaffRole.setText("");
                loadStaffList();
            } else {
                Toast.makeText(this, "Error adding staff", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdateStaff.setOnClickListener(v -> {
            String idStr = etStaffId.getText().toString().trim();
            String name = etStaffName.getText().toString().trim();
            String role = etStaffRole.getText().toString().trim();

            if (idStr.isEmpty() || name.isEmpty() || role.isEmpty()) {
                Toast.makeText(this, "Enter ID, name and role", Toast.LENGTH_SHORT).show();
                return;
            }

            int rows = dbHelper.updateStaff(Long.parseLong(idStr), name, role);

            if (rows > 0) {
                Toast.makeText(this, "Staff Updated Successfully", Toast.LENGTH_SHORT).show();
                loadStaffList();
            } else {
                Toast.makeText(this, "Update Failed. Staff not found", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteStaff.setOnClickListener(v -> {
            String idStr = etStaffId.getText().toString().trim();

            if (idStr.isEmpty()) {
                Toast.makeText(this, "Enter Staff ID", Toast.LENGTH_SHORT).show();
                return;
            }

            int rows = dbHelper.deleteStaff(Long.parseLong(idStr));

            if (rows > 0) {
                Toast.makeText(this, "Staff Deleted Successfully", Toast.LENGTH_SHORT).show();
                etStaffId.setText("");
                etStaffName.setText("");
                etStaffRole.setText("");
                loadStaffList();
            } else {
                Toast.makeText(this, "Delete Failed. Staff not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStaffList() {
        staffList.clear();
        Cursor cursor = dbHelper.getAllStaff();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_NAME));
                String role = cursor.getString(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ROLE));
                staffList.add("ID:" + id + " Name:" + name + " Role:" + role);
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }
}
