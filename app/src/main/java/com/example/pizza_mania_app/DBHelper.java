package com.example.pizza_mania_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pizzamania.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "userId TEXT PRIMARY KEY," +
                "name TEXT," +
                "email TEXT," +
                "phone TEXT," +
                "location TEXT," +
                "default_address TEXT," +
                "addresses TEXT," +
                "default_branch TEXT," +
                "createdAt TEXT," +
                "updatedAt TEXT" +
                ");");

        // Menu items table
        db.execSQL("CREATE TABLE IF NOT EXISTS menu_items (" +
                "itemId TEXT PRIMARY KEY," +
                "name TEXT," +
                "price REAL," +
                "imageUrl TEXT," +
                "category TEXT," +
                "description TEXT," +
                "branchId TEXT," +
                "status TEXT DEFAULT 'available'," +
                "createdAt TEXT," +
                "updatedAt TEXT" +
                ");");

        // Cart table
        db.execSQL("CREATE TABLE IF NOT EXISTS cart (" +
                "itemId TEXT PRIMARY KEY," +
                "name TEXT," +
                "price REAL," +
                "qty INTEGER," +
                "branchId TEXT" +
                ");");

        // Orders table
        db.execSQL("CREATE TABLE IF NOT EXISTS orders (" +
                "orderId TEXT PRIMARY KEY," +
                "customerId TEXT," +
                "items TEXT," +
                "total REAL," +
                "payment_method TEXT," +
                "delivery_address TEXT," +
                "delivery_location TEXT," +
                "branchId TEXT," +
                "status TEXT," +
                "assigned_deliveryId TEXT," +
                "createdAt TEXT," +
                "updatedAt TEXT" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS menu_items");
        db.execSQL("DROP TABLE IF EXISTS cart");
        db.execSQL("DROP TABLE IF EXISTS orders");
        onCreate(db);
    }
}
