package com.example.pizza_mania_app;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class welcomePage extends AppCompatActivity {

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainWelcome), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize DB when app opens
        createDatabase();

        Button btnLoginWelcome = findViewById(R.id.btnLoginWelcome);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvGuest = findViewById(R.id.tvGuest);

        btnLoginWelcome.setOnClickListener(v -> {
            startActivity(new Intent(welcomePage.this, loginPage.class));
        });

        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(welcomePage.this, signUp.class));
        });

        tvGuest.setOnClickListener(v -> {
            startActivity(new Intent(welcomePage.this, menu.class));
        });
    }

    /**
     * Create DB tables if not exist. Add missing columns for older DBs.
     */
    private void createDatabase() {
        try {
            db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

            // Users table
            db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT UNIQUE, " +
                    "phone TEXT, " +
                    "profile_pic TEXT, " +
                    "password TEXT, " +
                    "role TEXT)");

            // Add 'profile_pic' if missing in old DB
            try {
                db.execSQL("ALTER TABLE users ADD COLUMN profile_pic TEXT");
            } catch (Exception ignored) {}

            // Orders table
            db.execSQL("CREATE TABLE IF NOT EXISTS orders (" +
                    "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "customer_name TEXT, " +
                    "order_cart TEXT, " +
                    "order_address TEXT, " +
                    "address_latitude REAL, " +
                    "address_longitude REAL, " +
                    "payment_method TEXT, " +
                    "order_date TEXT, " +
                    "order_time TEXT, " +
                    "total REAL, " +
                    "order_status TEXT)");

            // Delivery table
            db.execSQL("CREATE TABLE IF NOT EXISTS delivery (" +
                    "delivery_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER, " +
                    "driver_id INTEGER, " +
                    "status TEXT, " +
                    "eta TEXT, " +
                    "FOREIGN KEY(order_id) REFERENCES orders(order_id), " +
                    "FOREIGN KEY(driver_id) REFERENCES users(user_id))");

            // Payments table
            db.execSQL("CREATE TABLE IF NOT EXISTS payments (" +
                    "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER, " +
                    "amount REAL, " +
                    "payment_date TEXT, " +
                    "payment_status TEXT, " +
                    "FOREIGN KEY(order_id) REFERENCES orders(order_id))");

            // Insert sample users if empty
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", null);
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                insertSampleUsers();
            }
            cursor.close();

            Toast.makeText(this, "Database ready ✅", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "DB Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void insertSampleUsers() {
        ContentValues values = new ContentValues();

        // Admin
        values.put("name", "Admin User");
        values.put("email", "admin@pizza.com");
        values.put("phone", "0711111111");
        values.put("password", "admin123");
        values.put("role", "admin");
        db.insert("users", null, values);

        // Driver
        values.clear();
        values.put("name", "Driver One");
        values.put("email", "driver@pizza.com");
        values.put("phone", "0722222222");
        values.put("password", "driver123");
        values.put("role", "driver");
        db.insert("users", null, values);
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }
}
