package com.example.pizza_mania_app.admin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pizza_mania.db";
    private static final int DATABASE_VERSION = 2;

    // ===== STAFF TABLE =====
    public static final String TABLE_STAFF = "staff";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ROLE = "role";

    // ===== CUSTOMER TABLE =====
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String COLUMN_PHONE = "phone";

    // ===== ITEMS TABLE =====
    public static final String TABLE_ITEMS = "items";
    public static final String COLUMN_ITEM_ID = "id";
    public static final String COLUMN_ITEM_NAME = "name";
    public static final String COLUMN_ITEM_PRICE = "price";

    // ===== CREATE TABLE QUERIES =====
    private static final String CREATE_STAFF_TABLE =
            "CREATE TABLE " + TABLE_STAFF + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_ROLE + " TEXT);";

    private static final String CREATE_CUSTOMERS_TABLE =
            "CREATE TABLE " + TABLE_CUSTOMERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_PHONE + " TEXT);";

    private static final String CREATE_ITEMS_TABLE =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
                    COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ITEM_NAME + " TEXT, " +
                    COLUMN_ITEM_PRICE + " REAL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STAFF_TABLE);
        db.execSQL(CREATE_CUSTOMERS_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STAFF);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    // ===== STAFF METHODS =====
    public long addStaff(String name, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_ROLE, role);
        return db.insert(TABLE_STAFF, null, values);
    }

    public int updateStaff(long id, String name, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_ROLE, role);
        return db.update(TABLE_STAFF, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteStaff(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_STAFF, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor getAllStaff() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_STAFF, null);
    }

    // ===== CUSTOMER METHODS =====
    public long addCustomer(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);
        return db.insert(TABLE_CUSTOMERS, null, values);
    }

    public int updateCustomer(long id, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);
        return db.update(TABLE_CUSTOMERS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteCustomer(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CUSTOMERS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor getAllCustomers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CUSTOMERS, null);
    }

    public Cursor searchCustomersByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CUSTOMERS + " WHERE " + COLUMN_NAME + " LIKE ?", new String[]{"%" + name + "%"});
    }

    // ===== ITEM METHODS =====
    public long addItem(String name, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, name);
        values.put(COLUMN_ITEM_PRICE, price);
        return db.insert(TABLE_ITEMS, null, values);
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);
    }

    public Cursor searchItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " WHERE " + COLUMN_ITEM_NAME + " LIKE ?", new String[]{"%" + name + "%"});
    }

    public Cursor searchItemById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " WHERE " + COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int updateItem(long id, String name, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, name);
        values.put(COLUMN_ITEM_PRICE, price);
        return db.update(TABLE_ITEMS, values, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ITEMS, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(id)});
    }
}
