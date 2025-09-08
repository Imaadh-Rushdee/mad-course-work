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

        // Delete old database for fresh start (optional)
        deleteDatabase("pizza_mania.db");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainWelcome), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize DB
        createDatabase();

        Button btnLoginWelcome = findViewById(R.id.btnLoginWelcome);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvGuest = findViewById(R.id.tvGuest);

        btnLoginWelcome.setOnClickListener(v -> startActivity(new Intent(welcomePage.this, loginPage.class)));
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(welcomePage.this, signUp.class)));
        tvGuest.setOnClickListener(v -> {
            Intent intent = new Intent(welcomePage.this, menu.class);
            intent.putExtra("userRole", "guest");
            intent.putExtra("branchId", 1); // default guest branch
            startActivity(intent);
        });
    }

    /** DB Creation + Sample Inserts */
    private void createDatabase() {
        try {
            db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

            // Branch table
            db.execSQL("CREATE TABLE IF NOT EXISTS branches (" +
                    "branch_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "branch_name TEXT NOT NULL, " +
                    "branch_address TEXT)");

            // Users table
            db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT UNIQUE, " +
                    "phone TEXT, " +
                    "password TEXT, " +
                    "role TEXT, " +
                    "address TEXT, " +
                    "profile_pic BLOB, " +
                    "branch_id INTEGER, " +
                    "FOREIGN KEY(branch_id) REFERENCES branches(branch_id))");

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
                    "order_status TEXT, " +
                    "order_type TEXT, " +
                    "branch_id INTEGER, " +
                    "FOREIGN KEY(branch_id) REFERENCES branches(branch_id))");

            // Payments table
            db.execSQL("CREATE TABLE IF NOT EXISTS payments (" +
                    "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER, " +
                    "amount REAL, " +
                    "payment_date TEXT, " +
                    "payment_status TEXT, " +
                    "FOREIGN KEY(order_id) REFERENCES orders(order_id))");

            // Menu table
            db.execSQL("CREATE TABLE IF NOT EXISTS menu_items (" +
                    "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "description TEXT, " +
                    "price REAL, " +
                    "image BLOB, " +
                    "branch_id INTEGER, " +
                    "FOREIGN KEY(branch_id) REFERENCES branches(branch_id))");

            // Insert sample data if tables empty
            insertSampleBranches();
            insertSampleUsers();
            insertSampleOrders();
            insertSampleMenuItems();

            Toast.makeText(this, "Database ready ✅", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "DB Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void insertSampleBranches() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM branches", null);
        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
            ContentValues values = new ContentValues();
            values.put("branch_name", "Colombo Branch");
            values.put("branch_address", "Colombo 07, Sri Lanka");
            db.insert("branches", null, values);

            values.clear();
            values.put("branch_name", "Kandy Branch");
            values.put("branch_address", "Kandy City, Sri Lanka");
            db.insert("branches", null, values);
        }
        cursor.close();
    }

    private void insertSampleUsers() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", null);
        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
            ContentValues values = new ContentValues();
            // Admin
            values.put("name", "Admin User");
            values.put("email", "admin@pizza.com");
            values.put("phone", "0711111111");
            values.put("password", "admin123");
            values.put("role", "admin");
            values.put("branch_id", 1);
            db.insert("users", null, values);

            // Driver 1
            values.clear();
            values.put("name", "Driver One");
            values.put("email", "driver@pizza.com");
            values.put("phone", "0722222222");
            values.put("password", "driver123");
            values.put("role", "driver");
            values.put("branch_id", 1);
            db.insert("users", null, values);

            // Driver 2
            values.clear();
            values.put("name", "Driver Two");
            values.put("email", "driver2@pizza.com");
            values.put("phone", "0733333333");
            values.put("password", "driver456");
            values.put("role", "driver");
            values.put("branch_id", 2);
            db.insert("users", null, values);
        }
        cursor.close();
    }

    private void insertSampleOrders() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM orders", null);
        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
            insertOrder("Test Customer 1", "1x Margherita, 2x Pepperoni", 6.6844, 80.4152, 1500, "Cash on Delivery", 1);
            insertOrder("Test Customer 2", "2x Veggie, 1x Hawaiian", 6.9000, 79.8700, 2200, "Card", 1);
            insertOrder("Test Customer 3", "3x Cheese, 1x BBQ Chicken", 6.9150, 79.8500, 3100, "Cash on Delivery", 2);
        }
        cursor.close();
    }

    private void insertOrder(String name, String cart, double lat, double lng, double total, String paymentMethod, int branchId) {
        ContentValues orderValues = new ContentValues();
        orderValues.put("customer_name", name);
        orderValues.put("order_cart", cart);
        orderValues.put("order_address", "Some Address");
        orderValues.put("address_latitude", lat);
        orderValues.put("address_longitude", lng);
        orderValues.put("payment_method", paymentMethod);
        orderValues.put("order_date", "2025-09-06");
        orderValues.put("order_time", "12:30 PM");
        orderValues.put("total", total);
        orderValues.put("order_status", "ready");
        orderValues.put("order_type", "delivery");
        orderValues.put("branch_id", branchId);
        db.insert("orders", null, orderValues);
    }

    private void insertSampleMenuItems() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM menu_items", null);
        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
            ContentValues values = new ContentValues();
            // Colombo branch
            values.put("name", "Margherita Pizza");
            values.put("description", "Classic cheese & tomato");
            values.put("price", 1200);
            values.put("branch_id", 1);
            db.insert("menu_items", null, values);

            values.put("name", "Pepperoni Pizza");
            values.put("description", "Loaded with pepperoni");
            values.put("price", 1500);
            values.put("branch_id", 1);
            db.insert("menu_items", null, values);

            // Kandy branch
            values.put("name", "BBQ Chicken Pizza");
            values.put("description", "Chicken + BBQ sauce");
            values.put("price", 1800);
            values.put("branch_id", 2);
            db.insert("menu_items", null, values);
        }
        cursor.close();
    }

    public SQLiteDatabase getDatabase() { return db; }
}
