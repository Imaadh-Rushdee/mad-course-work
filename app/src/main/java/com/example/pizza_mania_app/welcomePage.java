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

        // Delete old database for fresh start
        deleteDatabase("pizza_mania.db");

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

        btnLoginWelcome.setOnClickListener(v -> startActivity(new Intent(welcomePage.this, loginPage.class)));
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(welcomePage.this, signUp.class)));
        tvGuest.setOnClickListener(v -> startActivity(new Intent(welcomePage.this, menu.class)));
    }

    /** Create DB tables and insert sample data */
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

            // Orders table with new column order_type
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
                    "order_status TEXT, " +          // status column
                    "order_type TEXT)");             // new column

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
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) insertSampleUsers();
            cursor.close();

            // Insert 3 sample orders with default status 'ready' and type 'delivery'
            insertSampleOrders();

            Toast.makeText(this, "Database ready ✅", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "DB Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** Add initial users */
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

    /** Add 3 sample orders (always) */
    private void insertSampleOrders() {
        insertOrder("Test Customer 1", "1x Margherita, 2x Pepperoni", 6.9271, 79.8612, 1500, "Cash on Delivery");
        insertOrder("Test Customer 2", "2x Veggie, 1x Hawaiian", 6.9000, 79.8700, 2200, "Card");
        insertOrder("Test Customer 3", "3x Cheese, 1x BBQ Chicken", 6.9150, 79.8500, 3100, "Cash on Delivery");
    }

    /** Helper to insert an order */
    private void insertOrder(String name, String cart, double lat, double lng, double total, String paymentMethod) {
        ContentValues orderValues = new ContentValues();
        orderValues.put("customer_name", name);
        orderValues.put("order_cart", cart);
        orderValues.put("order_address", "Some Address, Colombo");
        orderValues.put("address_latitude", lat);
        orderValues.put("address_longitude", lng);
        orderValues.put("payment_method", paymentMethod);
        orderValues.put("order_date", "2025-09-06");
        orderValues.put("order_time", "12:30 PM");
        orderValues.put("total", total);
        orderValues.put("order_status", "ready");   // default status
        orderValues.put("order_type", "delivery");  // default type

        long orderId = db.insert("orders", null, orderValues);
        if(orderId != -1){
            Toast.makeText(this, "Sample order added: " + name, Toast.LENGTH_SHORT).show();
        }
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }
}
