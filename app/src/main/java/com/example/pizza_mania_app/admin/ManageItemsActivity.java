package com.example.pizza_mania_app.admin;

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

    EditText etItemName, etItemPrice;
    Button btnAdd, btnRead, btnUpdate, btnDelete;
    RecyclerView recyclerView;
    ItemAdapter adapter;
    ArrayList<Item> itemList = new ArrayList<Item>();
    int selectedIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_items);

        etItemName = findViewById(R.id.etItemName);
        etItemPrice = findViewById(R.id.etItemPrice);
        btnAdd = findViewById(R.id.btnAddItem);
        btnRead = findViewById(R.id.btnReadItem);
        btnUpdate = findViewById(R.id.btnUpdateItem);
        btnDelete = findViewById(R.id.btnDeleteItem);
        recyclerView = findViewById(R.id.recyclerItems);

        adapter = new ItemAdapter(itemList, new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                selectedIndex = position;
                etItemName.setText(itemList.get(position).getName());
                etItemPrice.setText(String.valueOf(itemList.get(position).getPrice()));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addItem());
        btnRead.setOnClickListener(v -> readItems());
        btnUpdate.setOnClickListener(v -> updateItem());
        btnDelete.setOnClickListener(v -> deleteItem());
    }

    private void addItem() {
        String name = etItemName.getText().toString();
        String priceStr = etItemPrice.getText().toString();
        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show();
            return;
        }
        double price = Double.parseDouble(priceStr);
        itemList.add(new Item(name, price));
        adapter.notifyDataSetChanged();
        clearFields();
    }

    private void readItems() {
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Items refreshed", Toast.LENGTH_SHORT).show();
    }

    private void updateItem() {
        if (selectedIndex == -1) {
            Toast.makeText(this, "Select an item first", Toast.LENGTH_SHORT).show();
            return;
        }
        itemList.get(selectedIndex).setName(etItemName.getText().toString());
        itemList.get(selectedIndex).setPrice(Double.parseDouble(etItemPrice.getText().toString()));
        adapter.notifyDataSetChanged();
        clearFields();
        selectedIndex = -1;
    }

    private void deleteItem() {
        if (selectedIndex == -1) {
            Toast.makeText(this, "Select an item first", Toast.LENGTH_SHORT).show();
            return;
        }
        itemList.remove(selectedIndex);
        adapter.notifyDataSetChanged();
        clearFields();
        selectedIndex = -1;
    }

    private void clearFields() {
        etItemName.setText("");
        etItemPrice.setText("");
    }
}
