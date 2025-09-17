package com.example.pizza_mania_app;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
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

        createDatabase();

        Button btnLoginWelcome = findViewById(R.id.btnLoginWelcome);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        btnLoginWelcome.setOnClickListener(v -> startActivity(new Intent(welcomePage.this, loginPage.class)));
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(welcomePage.this, signUp.class)));
    }

    private void createDatabase() {
        try {
            db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

            db.execSQL("CREATE TABLE IF NOT EXISTS branches (" +
                    "branch_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "branch_name TEXT NOT NULL, " +
                    "branch_address TEXT)");

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

            db.execSQL("CREATE TABLE IF NOT EXISTS payments (" +
                    "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER, " +
                    "amount REAL, " +
                    "payment_date TEXT, " +
                    "payment_status TEXT, " +
                    "FOREIGN KEY(order_id) REFERENCES orders(order_id))");

            db.execSQL("CREATE TABLE IF NOT EXISTS carts (" +
                    "cart_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "created_at TEXT DEFAULT (datetime('now','localtime')), " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id))");

            db.execSQL("CREATE TABLE IF NOT EXISTS cart_items (" +
                    "cart_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "cart_id INTEGER NOT NULL, " +
                    "item_id INTEGER NOT NULL, " +
                    "quantity INTEGER DEFAULT 1, " +
                    "checked INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(cart_id) REFERENCES carts(cart_id), " +
                    "FOREIGN KEY(item_id) REFERENCES menu_items(item_id))");

            db.execSQL("CREATE TABLE IF NOT EXISTS menu_items (" +
                    "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "description TEXT, " +
                    "price REAL, " +
                    "image BLOB, " +
                    "branch_id INTEGER, " +
                    "FOREIGN KEY(branch_id) REFERENCES branches(branch_id))");

            db.execSQL("DELETE FROM cart_items");
            db.execSQL("DELETE FROM carts");
            db.execSQL("DELETE FROM payments");
            db.execSQL("DELETE FROM orders");
            db.execSQL("DELETE FROM menu_items");
            db.execSQL("DELETE FROM users");
            db.execSQL("DELETE FROM branches");

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
        ContentValues values = new ContentValues();
        values.put("branch_name", "Colombo Branch");
        values.put("branch_address", "Colombo 07, Sri Lanka");
        db.insert("branches", null, values);

        values.clear();
        values.put("branch_name", "Galle Branch");
        values.put("branch_address", "Galle City, Sri Lanka");
        db.insert("branches", null, values);
    }

    private void insertSampleUsers() {
        ContentValues values = new ContentValues();
        values.put("name", "Admin User");
        values.put("email", "admin@pizza.com");
        values.put("phone", "0711111111");
        values.put("password", "admin123");
        values.put("role", "admin");
        values.put("branch_id", 2);
        db.insert("users", null, values);

        values.clear();
        values.put("name", "Admin User");
        values.put("email", "admin123@pizza.com");
        values.put("phone", "0711111111");
        values.put("password", "123");
        values.put("role", "admin");
        values.put("branch_id", 1);
        db.insert("users", null, values);

        values.clear();
        values.put("name", "Driver One");
        values.put("email", "driver@pizza.com");
        values.put("phone", "0722222222");
        values.put("password", "driver123");
        values.put("role", "driver");
        values.put("branch_id", 1);
        db.insert("users", null, values);

        values.clear();
        values.put("name", "Customer");
        values.put("email", "cu");
        values.put("phone", "0722222222");
        values.put("password", "123");
        values.put("role", "customer");
        values.put("address", "92 Raja Mawatha Rd, Dehiwala-Mount Lavinia");
        values.put("branch_id", 1);
        db.insert("users", null, values);

        values.clear();
        values.put("name", "Customer");
        values.put("email", "cus");
        values.put("phone", "0722222222");
        values.put("password", "123");
        values.put("role", "customer");
        values.put("address", "92 Raja Mawatha Rd, Dehiwala-Mount Lavinia");
        values.put("branch_id", 2);
        db.insert("users", null, values);

        values.clear();
        values.put("name", "Driver Two");
        values.put("email", "driver2@pizza.com");
        values.put("phone", "0733333333");
        values.put("password", "driver456");
        values.put("role", "driver");
        values.put("branch_id", 2);
        db.insert("users", null, values);
    }

    private void insertSampleOrders() {
        insertOrder("Test Customer 1", "1x Margherita, 2x Pepperoni", 6.6844, 80.4152, 1500, "Cash on Delivery", 1);
        insertOrder("Test Customer 2", "2x Veggie, 1x Hawaiian", 6.9000, 79.8700, 2200, "Card", 1);
        insertOrder("Test Customer 3", "3x Cheese, 1x BBQ Chicken", 6.9150, 79.8500, 3100, "Cash on Delivery", 2);
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
        ContentValues values = new ContentValues();
        values.put("name", "Margherita Pizza");
        values.put("description", "Classic cheese & tomato");
        values.put("price", 1200);
        values.put("branch_id", 1);
        db.insert("menu_items", null, values);

        values.clear();
        values.put("name", "Pepperoni Pizza");
        values.put("description", "Loaded with pepperoni");
        values.put("price", 1500);
        values.put("branch_id", 1);
        db.insert("menu_items", null, values);

        values.clear();
        values.put("name", "BBQ Chicken Pizza");
        values.put("description", "Chicken + BBQ sauce");
        values.put("price", 1800);
        values.put("branch_id", 2);
        db.insert("menu_items", null, values);
    }

    public SQLiteDatabase getDatabase() { return db; }
}
