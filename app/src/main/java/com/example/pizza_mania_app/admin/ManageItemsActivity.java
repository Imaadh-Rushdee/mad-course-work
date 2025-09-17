package com.example.pizza_mania_app.admin;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pizza_mania_app.R;

import java.util.ArrayList;

public class ManageItemsActivity extends AppCompatActivity {

    EditText etItemId, etItemName, etItemPrice;
    Button btnAdd, btnRead, btnUpdate, btnDelete, btnSearchById, btnSearchByName;
    RecyclerView recyclerView;
    ItemAdapter adapter;
    ArrayList<Item> itemList = new ArrayList<>();

    com.example.pizza_mania_app.admin.DatabaseHelper db;

    long selectedId = -1; // to keep track of selected item ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_items);

        db = new com.example.pizza_mania_app.admin.DatabaseHelper(this);

        etItemId = findViewById(R.id.etItemId); // <-- add this EditText in XML
        etItemName = findViewById(R.id.etItemName);
        etItemPrice = findViewById(R.id.etItemPrice);

        btnAdd = findViewById(R.id.btnAddItem);
        btnRead = findViewById(R.id.btnReadItem);
        btnUpdate = findViewById(R.id.btnUpdateItem);
        btnDelete = findViewById(R.id.btnDeleteItem);
        btnSearchById = findViewById(R.id.btnSearchById);     // <-- add in XML
        btnSearchByName = findViewById(R.id.btnSearchByName); // <-- add in XML

        recyclerView = findViewById(R.id.recyclerItems);

        adapter = new ItemAdapter(itemList, position -> {
            Item item = itemList.get(position);
            selectedId = item.getId();
            etItemId.setText(String.valueOf(item.getId()));
            etItemName.setText(item.getName());
            etItemPrice.setText(String.valueOf(item.getPrice()));
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addItem());
        btnRead.setOnClickListener(v -> loadItems());
        btnUpdate.setOnClickListener(v -> updateItem());
        btnDelete.setOnClickListener(v -> deleteItem());
        btnSearchById.setOnClickListener(v -> searchItemById());
        btnSearchByName.setOnClickListener(v -> searchItemByName());

        loadItems(); // load all items at start
    }

    private void addItem() {
        String name = etItemName.getText().toString().trim();
        String priceStr = etItemPrice.getText().toString().trim();
        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show();
            return;
        }
        double price = Double.parseDouble(priceStr);
        long id = db.addItem(name, price);
        if (id != -1) {
            Toast.makeText(this, "Item added with ID: " + id, Toast.LENGTH_SHORT).show();
            loadItems();
            clearFields();
        } else {
            Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadItems() {
        itemList.clear();
        Cursor cursor = db.getAllItems();
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ITEM_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ITEM_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ITEM_PRICE));
                itemList.add(new Item(id, name, price));
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void updateItem() {
        if (selectedId == -1) {
            Toast.makeText(this, "Select an item first", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = etItemName.getText().toString().trim();
        String priceStr = etItemPrice.getText().toString().trim();
        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show();
            return;
        }
        double price = Double.parseDouble(priceStr);
        int rows = db.updateItem(selectedId, name, price);
        if (rows > 0) {
            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
            loadItems();
            clearFields();
            selectedId = -1;
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteItem() {
        if (selectedId == -1) {
            Toast.makeText(this, "Select an item first", Toast.LENGTH_SHORT).show();
            return;
        }
        int rows = db.deleteItem(selectedId);
        if (rows > 0) {
            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
            loadItems();
            clearFields();
            selectedId = -1;
        } else {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchItemById() {
        String idStr = etItemId.getText().toString().trim();
        if (idStr.isEmpty()) {
            Toast.makeText(this, "Enter Item ID", Toast.LENGTH_SHORT).show();
            return;
        }
        long id = Long.parseLong(idStr);
        Cursor cursor = db.searchItemById(id);
        itemList.clear();
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ITEM_NAME));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ITEM_PRICE));
            itemList.add(new Item(id, name, price));
        } else {
            Toast.makeText(this, "No item found with ID " + id, Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void searchItemByName() {
        String nameQuery = etItemName.getText().toString().trim();
        if (nameQuery.isEmpty()) {
            Toast.makeText(this, "Enter name to search", Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor cursor = db.searchItemByName(nameQuery);
        itemList.clear();
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ITEM_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ITEM_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(com.example.pizza_mania_app.admin.DatabaseHelper.COLUMN_ITEM_PRICE));
                itemList.add(new Item(id, name, price));
            } while (cursor.moveToNext());
        } else {
            Toast.makeText(this, "No items found", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void clearFields() {
        etItemId.setText("");
        etItemName.setText("");
        etItemPrice.setText("");
    }
}
