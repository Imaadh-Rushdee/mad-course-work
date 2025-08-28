package com.example.pizza_mania_app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = "DBSetup";
    private FirebaseFirestore db;
    private DBHelper dbHelper;
    private String currentUserId = "user_001";  // Example user
    private String currentBranchId = "branch_001"; // Example branch

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        dbHelper = new DBHelper(this);

        // Create SQLite tables
        dbHelper.getWritableDatabase();

        // Firestore setup (run once)
        createFirestoreCollections();

        // Initial Firestore → SQLite sync
        syncMenuToSQLite();
        syncUserToSQLite();

        // Add real-time listeners
        listenMenuRealtime();
        listenUserRealtime();
    }

    // ---------------- Firestore Setup ----------------
    private void createFirestoreCollections() {
        // Branch
        Map<String, Object> branch = new HashMap<>();
        branch.put("name", "Colombo Branch");
        branch.put("location", Map.of("lat", 6.9271, "lng", 79.8612));
        branch.put("phone", "+94111234567");
        branch.put("createdAt", FieldValue.serverTimestamp());
        branch.put("updatedAt", FieldValue.serverTimestamp());
        db.collection("branches").document(currentBranchId).set(branch);

        // Menu items
        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "Margherita Pizza");
        item1.put("price", 500);
        item1.put("imageUrl", "https://firebasestorage.googleapis.com/.../margherita.jpg");
        item1.put("category", "Pizza");
        item1.put("description", "Classic Margherita with fresh basil");
        item1.put("branchId", currentBranchId);
        item1.put("status", "available");
        item1.put("createdAt", FieldValue.serverTimestamp());
        item1.put("updatedAt", FieldValue.serverTimestamp());

        Map<String, Object> item2 = new HashMap<>();
        item2.put("name", "Pepperoni Pizza");
        item2.put("price", 700);
        item2.put("imageUrl", "https://firebasestorage.googleapis.com/.../pepperoni.jpg");
        item2.put("category", "Pizza");
        item2.put("description", "Spicy Pepperoni Pizza");
        item2.put("branchId", currentBranchId);
        item2.put("status", "available");
        item2.put("createdAt", FieldValue.serverTimestamp());
        item2.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("menu_items").document("item_001").set(item1);
        db.collection("menu_items").document("item_002").set(item2);

        // User
        Map<String, Object> user = new HashMap<>();
        user.put("name", "John Doe");
        user.put("email", "john@example.com");
        user.put("phone", "+94771234567");
        user.put("role", "customer");
        user.put("location", Map.of("lat", 6.9271, "lng", 79.8612));
        user.put("default_address", "123 Main Street, Colombo");
        user.put("addresses", List.of("123 Main Street, Colombo", "456 Another St"));
        user.put("default_branch", currentBranchId);
        user.put("createdAt", FieldValue.serverTimestamp());
        user.put("updatedAt", FieldValue.serverTimestamp());
        db.collection("users").document(currentUserId).set(user);
    }

    // ---------------- Firestore → SQLite Sync ----------------
    private void syncMenuToSQLite() {
        db.collection("menu_items").whereEqualTo("branchId", currentBranchId)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    SQLiteDatabase sqlDb = dbHelper.getWritableDatabase();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ContentValues values = new ContentValues();
                        values.put("itemId", doc.getId());
                        values.put("name", doc.getString("name"));
                        values.put("price", doc.getDouble("price"));
                        values.put("imageUrl", doc.getString("imageUrl"));
                        values.put("category", doc.getString("category"));
                        values.put("description", doc.getString("description"));
                        values.put("branchId", doc.getString("branchId"));
                        values.put("status", doc.getString("status"));
                        sqlDb.insertWithOnConflict("menu_items", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }
                    sqlDb.close();
                    Log.d(TAG, "Menu items synced to SQLite");
                });
    }

    private void syncUserToSQLite() {
        db.collection("users").document(currentUserId)
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        SQLiteDatabase sqlDb = dbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("userId", doc.getId());
                        values.put("name", doc.getString("name"));
                        values.put("email", doc.getString("email"));
                        values.put("phone", doc.getString("phone"));
                        values.put("location", doc.getData().get("location").toString());
                        values.put("default_address", doc.getString("default_address"));
                        values.put("addresses", doc.getData().get("addresses").toString());
                        values.put("default_branch", doc.getString("default_branch"));
                        sqlDb.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                        sqlDb.close();
                        Log.d(TAG, "User info synced to SQLite");
                    }
                });
    }

    // ---------------- Real-time listeners ----------------
    private void listenMenuRealtime() {
        db.collection("menu_items").whereEqualTo("branchId", currentBranchId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    SQLiteDatabase sqlDb = dbHelper.getWritableDatabase();
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        DocumentSnapshot doc = dc.getDocument();
                        ContentValues values = new ContentValues();
                        values.put("itemId", doc.getId());
                        values.put("name", doc.getString("name"));
                        values.put("price", doc.getDouble("price"));
                        values.put("imageUrl", doc.getString("imageUrl"));
                        values.put("category", doc.getString("category"));
                        values.put("description", doc.getString("description"));
                        values.put("branchId", doc.getString("branchId"));
                        values.put("status", doc.getString("status"));
                        sqlDb.insertWithOnConflict("menu_items", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }
                    sqlDb.close();
                    Log.d(TAG, "Menu real-time update synced to SQLite");
                });
    }

    private void listenUserRealtime() {
        db.collection("users").document(currentUserId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null || !doc.exists()) return;
                    SQLiteDatabase sqlDb = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("userId", doc.getId());
                    values.put("name", doc.getString("name"));
                    values.put("email", doc.getString("email"));
                    values.put("phone", doc.getString("phone"));
                    values.put("location", doc.getData().get("location").toString());
                    values.put("default_address", doc.getString("default_address"));
                    values.put("addresses", doc.getData().get("addresses").toString());
                    values.put("default_branch", doc.getString("default_branch"));
                    sqlDb.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    sqlDb.close();
                    Log.d(TAG, "User real-time update synced to SQLite");
                });
    }

    // ---------------- Push cart → Firestore ----------------
    // ---------------- Push cart → Firestore ----------------
    private void pushCartToFirestore(String customerId, String branchId, String paymentMethod, String deliveryAddress) {
        SQLiteDatabase sqlDb = dbHelper.getReadableDatabase();
        Cursor cursor = sqlDb.rawQuery("SELECT * FROM cart", null);
        List<Map<String, Object>> items = new ArrayList<>();

        while (cursor.moveToNext()) {
            int idxItemId = cursor.getColumnIndex("itemId");
            int idxQty = cursor.getColumnIndex("qty");
            int idxPrice = cursor.getColumnIndex("price");
            int idxBranch = cursor.getColumnIndex("branchId");

            Map<String, Object> item = new HashMap<>();
            if (idxItemId != -1) item.put("itemId", cursor.getString(idxItemId));
            if (idxQty != -1) item.put("qty", cursor.getInt(idxQty));
            if (idxPrice != -1) item.put("price", cursor.getDouble(idxPrice));
            if (idxBranch != -1) item.put("branchId", cursor.getString(idxBranch));

            items.add(item);
        }
        cursor.close();

        if (items.isEmpty()) return;

        Map<String, Object> order = new HashMap<>();
        order.put("customerId", customerId);
        order.put("items", items);

        double total = 0;
        for (Map<String, Object> it : items) {
            Object priceObj = it.get("price");
            Object qtyObj = it.get("qty");
            double price = priceObj instanceof Double ? (Double) priceObj : ((Integer) priceObj).doubleValue();
            int qty = qtyObj instanceof Integer ? (Integer) qtyObj : ((Long) qtyObj).intValue();
            total += price * qty;
        }
        order.put("total", total);
        order.put("payment_method", paymentMethod);
        order.put("delivery_address", deliveryAddress);
        order.put("branchId", branchId);
        order.put("status", "Pending");
        order.put("assigned_deliveryId", null);
        order.put("createdAt", FieldValue.serverTimestamp());
        order.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("orders").add(order).addOnSuccessListener(docRef -> {
            Log.d(TAG, "Cart pushed as order to Firestore");
            sqlDb.execSQL("DELETE FROM cart"); // clear cart after push
        });
    }

}
