package com.example.pizza_mania_app.admin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class OrderDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "orders.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ORDERS = "orders";

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_STATUS = "status";

    public OrderDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ORDERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_STATUS + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        onCreate(db);
    }

    // Insert order (auto ID)
    public long addOrder(String name, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_STATUS, status);
        return db.insert(TABLE_ORDERS, null, values);
    }

    // Update order
    public int updateOrder(long id, String name, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_STATUS, status);
        return db.update(TABLE_ORDERS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Delete order
    public int deleteOrder(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ORDERS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Get all orders
    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ORDERS, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Order(
                        cursor.getLong(0),  // id
                        cursor.getString(1), // name
                        cursor.getString(2)  // status
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // Get pending orders
    public List<Order> getPendingOrders() {
        return getOrdersByStatus("Pending");
    }

    // Get completed orders
    public List<Order> getCompletedOrders() {
        return getOrdersByStatus("Completed");
    }

    // Helper to fetch by status
    private List<Order> getOrdersByStatus(String status) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " WHERE " + COL_STATUS + "=?", new String[]{status});
        if (cursor.moveToFirst()) {
            do {
                list.add(new Order(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
