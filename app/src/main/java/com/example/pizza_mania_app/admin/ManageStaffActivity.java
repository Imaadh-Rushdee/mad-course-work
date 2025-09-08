package com.example.pizza_mania_app.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;

public class ManageStaffActivity extends AppCompatActivity {

    EditText etStaffName, etStaffRole;
    Button btnAddStaff, btnUpdateStaff, btnDeleteStaff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_staff);

        etStaffName = findViewById(R.id.etStaffName);
        etStaffRole = findViewById(R.id.etStaffRole);
        btnAddStaff = findViewById(R.id.btnAddStaff);
        btnUpdateStaff = findViewById(R.id.btnUpdateStaff);
        btnDeleteStaff = findViewById(R.id.btnDeleteStaff);

        btnAddStaff.setOnClickListener(v ->
                Toast.makeText(this, "Staff Added: " + etStaffName.getText(), Toast.LENGTH_SHORT).show());

        btnUpdateStaff.setOnClickListener(v ->
                Toast.makeText(this, "Staff Updated", Toast.LENGTH_SHORT).show());

        btnDeleteStaff.setOnClickListener(v ->
                Toast.makeText(this, "Staff Deleted", Toast.LENGTH_SHORT).show());
    }
}
