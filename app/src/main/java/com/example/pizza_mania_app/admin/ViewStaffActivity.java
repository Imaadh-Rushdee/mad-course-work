package com.example.pizza_mania_app.admin;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;

import java.util.ArrayList;

public class ViewStaffActivity extends AppCompatActivity {

    com.example.pizza_mania_app.admin.DatabaseHelper dbHelper;
    ListView listViewStaff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_staff);

        listViewStaff = findViewById(R.id.listViewStaff);
        dbHelper = new com.example.pizza_mania_app.admin.DatabaseHelper(this);

        loadStaff();
    }

    private void loadStaff() {
        Cursor cursor = dbHelper.getAllStaff();
        ArrayList<String> staffList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No staff found", Toast.LENGTH_SHORT).show();
            return;
        }

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_NAME));
            String role = cursor.getString(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ROLE));

            staffList.add("ID: " + id + " | Name: " + name + " | Role: " + role);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, staffList);

        listViewStaff.setAdapter(adapter);
    }
}
