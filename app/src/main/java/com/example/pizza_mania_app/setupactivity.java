package com.example.pizza_mania_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class setupactivity extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "pizza_mania.db"; // Database name
    private static final int DATABASE_VERSION = 1; // Increase this when adding new tables or columns

    // Example Table Names
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String TABLE_MENU = "menu_items";

    // Example SQL for Orders Table
    private static final String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "customer_id INTEGER, " +
            "status TEXT, " +
            "total_price REAL" +
            ");";

    // Example SQL for Customers Table
    private static final String CREATE_CUSTOMERS_TABLE = "CREATE TABLE " + TABLE_CUSTOMERS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "phone TEXT, " +
            "address TEXT" +
            ");";

    // Example SQL for Menu Table
    private static final String CREATE_MENU_TABLE = "CREATE TABLE " + TABLE_MENU + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "item_name TEXT, " +
            "price REAL, " +
            "branch_id TEXT" +
            ");";

    public setupactivity(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create initial tables
        db.execSQL(CREATE_ORDERS_TABLE);
        db.execSQL(CREATE_CUSTOMERS_TABLE);
        db.execSQL(CREATE_MENU_TABLE);

        Log.d("DB_SETUP", "Database tables created successfully!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables when upgrading schema
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU);
        onCreate(db); // Recreate tables
    }

    /*
     * ✅ HOW TO USE THIS CLASS:
     *
     * 1. Initialize in your Activity:
     *    setupactivity dbHelper = new setupactivity(this);
     *    SQLiteDatabase db = dbHelper.getWritableDatabase();
     *
     * 2. To Add a New Table:
     *    - Create a new CREATE TABLE SQL string like CREATE_NEW_TABLE
     *    - Add db.execSQL(CREATE_NEW_TABLE); inside onCreate()
     *    - Increase DATABASE_VERSION by 1
     *
     * 3. To Insert/Update/Delete:
     *    - Use db.insert(), db.update(), db.delete()
     *
     * 4. To Query Data:
     *    - Use db.rawQuery("SELECT * FROM table_name", null);
     */
}
