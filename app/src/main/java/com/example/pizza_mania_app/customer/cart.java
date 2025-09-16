package com.example.pizza_mania_app.customer;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;

import java.util.HashMap;

public class cart extends AppCompatActivity {

    private SQLiteDatabase db;
    private LinearLayout cartContainer;
    private TextView tvTotal, tvOrderType, tvAddress, tvBranch;
    private Button btnBack, btnPlaceOrder;
    private String userId, orderType, address, branch;
    private double lat = 0.0, lng = 0.0;

    private final HashMap<Integer, Double> itemTotals = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        userId = getIntent().getStringExtra("userId");
        orderType = getIntent().getStringExtra("orderType");
        address = getIntent().getStringExtra("userAddress");
        branch = getIntent().getStringExtra("branch");
        lat = getIntent().getDoubleExtra("lat", 0.0);
        lng = getIntent().getDoubleExtra("lng", 0.0);

        cartContainer = findViewById(R.id.cartContainer);
        tvTotal = findViewById(R.id.tvTotal);
        tvOrderType = findViewById(R.id.tvOrderType);
        tvAddress = findViewById(R.id.tvAddress);
        tvBranch = findViewById(R.id.tvBranch);
        btnBack = findViewById(R.id.btnBack);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        tvOrderType.setText("Order Type: " + (orderType != null ? orderType : "N/A"));
        tvAddress.setText("Address: " + (address != null ? address : "N/A"));
        tvBranch.setText("Branch: " + (branch != null ? branch : "N/A"));

        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        loadCartItems(userId);

        btnBack.setOnClickListener(v -> finish());

        btnPlaceOrder.setOnClickListener(v -> {
            if (itemTotals.isEmpty()) {
                Toast.makeText(this, "Please select at least one item.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(cart.this, confirmOrder.class);
            intent.putExtra("userId", userId);
            intent.putExtra("orderType", orderType);
            intent.putExtra("userAddress", address);
            startActivity(intent);
        });
    }

    private void loadCartItems(String userId) {
        cartContainer.removeAllViews();
        itemTotals.clear();

        Cursor cartCursor = db.rawQuery("SELECT cart_id FROM carts WHERE user_id=?", new String[]{userId});
        if (cartCursor.moveToFirst()) {
            int cartId = cartCursor.getInt(0);

            Cursor c = db.rawQuery(
                    "SELECT ci.cart_item_id, m.name, m.price, ci.quantity, ci.checked, m.image " +
                            "FROM cart_items ci " +
                            "JOIN menu_items m ON ci.item_id = m.item_id " +
                            "WHERE ci.cart_id=?",
                    new String[]{String.valueOf(cartId)});

            LayoutInflater inflater = LayoutInflater.from(this);

            while (c.moveToNext()) {
                int cartItemId = c.getInt(0);
                String name = c.getString(1);
                double price = c.getDouble(2);
                int qty = c.getInt(3);
                boolean checked = c.getInt(4) == 1;
                byte[] imageBytes = c.getBlob(5);

                LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.cart_item, cartContainer, false);
                ImageView ivItem = itemView.findViewById(R.id.itemImage);
                TextView tvName = itemView.findViewById(R.id.tvItemName);
                TextView tvPrice = itemView.findViewById(R.id.tvItemPrice);
                TextView tvQty = itemView.findViewById(R.id.tvItemQty);
                CheckBox cb = itemView.findViewById(R.id.cbSelect);

                tvName.setText(name);
                tvPrice.setText("Rs. " + price);
                tvQty.setText("Qty: " + qty);
                cb.setChecked(checked);

                if (imageBytes != null) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    ivItem.setImageBitmap(bmp);
                } else {
                    ivItem.setImageResource(R.drawable.logo);
                }

                if (checked) itemTotals.put(cartItemId, price * qty);

                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    db.execSQL("UPDATE cart_items SET checked=? WHERE cart_item_id=?", new Object[]{isChecked ? 1 : 0, cartItemId});
                    if (isChecked) itemTotals.put(cartItemId, price * qty);
                    else itemTotals.remove(cartItemId);
                    updateTotal();
                });

                cartContainer.addView(itemView);
            }

            c.close();
            updateTotal();
        } else {
            TextView emptyMsg = new TextView(this);
            emptyMsg.setText("Nothing to display here");
            emptyMsg.setTextSize(18f);
            emptyMsg.setPadding(16, 16, 16, 16);
            cartContainer.addView(emptyMsg);
            tvTotal.setText("Total: Rs. 0");
        }

        cartCursor.close();
    }

    private void updateTotal() {
        double total = 0;
        for (double val : itemTotals.values()) total += val;
        tvTotal.setText("Total: Rs. " + total);
    }
}
