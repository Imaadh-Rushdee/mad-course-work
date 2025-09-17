package com.example.pizza_mania_app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.admin.AdminDashboardActivity;
import com.example.pizza_mania_app.deliveryPartner.deliveryPartnerDashboard;

import java.util.concurrent.Executors;

public class loginPage extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        // Open database
        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });
    }

    public void sigUp(View view){
        Intent intent = new Intent(this, signUp.class);
        startActivity(intent);
    }
    private void loginUser(String email, String password) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Query user in background
            Cursor cursor = db.rawQuery(
                    "SELECT user_id, role, name, address, branch_id FROM users WHERE email=? AND password=?",
                    new String[]{email, password});

            boolean success = cursor.moveToFirst();

            int userId = -1, branchId = -1;
            String role = null, name = null, address = null;

            if (success) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                branchId = cursor.getInt(cursor.getColumnIndexOrThrow("branch_id"));
            }
            cursor.close();

            int finalUserId = userId;
            int finalBranchId = branchId;
            String finalRole = role;
            String finalName = name;
            String finalAddress = address;

            // Update UI on main thread
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "Welcome " + finalName, Toast.LENGTH_SHORT).show();

                    Intent intent;
                    switch (finalRole.toLowerCase()) {
                        case "admin":
                            intent = new Intent(loginPage.this, AdminDashboardActivity.class);
                            intent.putExtra("userRole", "admin");
                            intent.putExtra("branchId", finalBranchId);
                            break;
                        case "driver":
                            intent = new Intent(loginPage.this, deliveryPartnerDashboard.class);
                            intent.putExtra("partnerId", finalUserId);
                            intent.putExtra("branchId", finalBranchId);
                            break;
                        case "customer":
                            intent = new Intent(loginPage.this, OrderSetupActivity.class);
                            intent.putExtra("userRole", "customer");
                            intent.putExtra("userId", String.valueOf(finalUserId));
                            intent.putExtra("defaultBranchId", finalBranchId);
                            intent.putExtra("defaultAddress", finalAddress);
                            break;
                        default:
                            Toast.makeText(this, "Unknown role: " + finalRole, Toast.LENGTH_SHORT).show();
                            return;
                    }

                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
