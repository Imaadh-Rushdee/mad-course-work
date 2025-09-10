package com.example.pizza_mania_app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania_app.deliveryPartner.deliveryPartnerDashboard;

public class loginPage extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

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

    private void loginUser(String email, String password) {
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email=? AND password=?",
                new String[]{email, password});

        if (cursor.moveToFirst()) {
            String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
            int userBranchId = cursor.getInt(cursor.getColumnIndexOrThrow("branch_id")); // <-- get branch id

            Toast.makeText(this, "Welcome " + name, Toast.LENGTH_SHORT).show();

            Intent intent;
            switch (role.toLowerCase()) {
                case "admin":
                    intent = new Intent(loginPage.this, menu.class);
                    intent.putExtra("userRole", "admin");
                    intent.putExtra("branchId", userBranchId); // pass branchId
                    break;
                case "driver":
                    intent = new Intent(loginPage.this, deliveryPartnerDashboard.class);
                    intent.putExtra("partnerId", userId);
                    intent.putExtra("branchId", userBranchId); // optional for driver
                    break;
                case "customer":
                    intent = new Intent(loginPage.this, OrderSetupActivity.class);
                    intent.putExtra("userRole", "customer");
                    intent.putExtra("userId", String.valueOf(userId));
                    intent.putExtra("defaultBranchId", userBranchId); // pass branchId
                    intent.putExtra("defaultAddress", address); // pass branchId
                    break;
                default:
                    Toast.makeText(this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                    cursor.close();
                    return;
            }

            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }
}
