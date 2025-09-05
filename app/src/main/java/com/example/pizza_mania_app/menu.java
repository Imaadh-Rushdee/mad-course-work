package com.example.pizza_mania_app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class menu extends AppCompatActivity{

    private LinearLayout menuContainer;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        menuContainer = findViewById(R.id.menuContainer);

        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);
        createMenuTableIfNotExists();   // make sure table exists
        insertSampleMenuItems();        // only if empty
        loadMenuItems();
    }
    private void createMenuTableIfNotExists() {
        db.execSQL("CREATE TABLE IF NOT EXISTS menu_items (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "description TEXT, " +
                "price REAL)");
    }
    private void insertSampleMenuItems() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM menu_items", null);
        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
            db.execSQL("INSERT INTO menu_items (name, description, price) VALUES " +
                    "('Margherita Pizza', 'Classic cheese & tomato', 1200), " +
                    "('Pepperoni Pizza', 'Loaded with pepperoni', 1500), " +
                    "('BBQ Chicken Pizza', 'Chicken + BBQ sauce', 1800)");
        }
        cursor.close();
    }
    private void loadMenuItems() {
        Cursor cursor = db.rawQuery("SELECT * FROM menu_items", null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));

            addMenuItem(name, desc, price);
        }

        cursor.close();
    }
    private void addMenuItem(String name, String desc, double price) {
        LinearLayout card = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.menu_item, menuContainer, false);

        TextView tvName = card.findViewById(R.id.itemName);
        TextView tvDesc = card.findViewById(R.id.itemDesc);
        TextView tvPrice = card.findViewById(R.id.itemPrice);
        Button btnAdd = card.findViewById(R.id.btnAdd);

        tvName.setText(name);
        tvDesc.setText(desc);
        tvPrice.setText("Rs. " + price);

        btnAdd.setOnClickListener(v ->
                Toast.makeText(this, name + " added to cart!", Toast.LENGTH_SHORT).show()
        );

        menuContainer.addView(card);
    }
}
