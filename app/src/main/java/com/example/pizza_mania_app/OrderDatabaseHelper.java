package com.example.pizza_mania_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pizza_mania_app.admin.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pizza_mania.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_ORDERS = "orders";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "customer_name";
    private static final String COL_ORDER_STATUS = "order_status";

    public OrderDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ORDERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_ORDER_STATUS + " TEXT, " +
                "branch_id INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ORDERS + " ADD COLUMN " + COL_ORDER_STATUS + " TEXT DEFAULT 'pending'");
        }
    }

    // Add a new order
    public long addOrder(String name, String status, int branchId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_ORDER_STATUS, status);
        values.put("branch_id", branchId);
        return db.insert(TABLE_ORDERS, null, values);
    }

    // Update order status
    public int updateOrderStatus(long id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ORDER_STATUS, status);
        return db.update(TABLE_ORDERS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Delete order
    public int deleteOrder(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ORDERS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Get pending orders for a specific branch
    public List<Order> getPendingOrdersByBranch(int branchId) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ORDERS + " WHERE " + COL_ORDER_STATUS + "=? AND branch_id=?",
                new String[]{"pending", String.valueOf(branchId)}
        );

        if (cursor.moveToFirst()) {
            do {
                list.add(new Order(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_STATUS))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
