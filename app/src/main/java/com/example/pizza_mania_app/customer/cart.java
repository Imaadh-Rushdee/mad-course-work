package com.example.pizza_mania_app.customer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;

public class cart extends AppCompatActivity {

    private SQLiteDatabase db;
    private LinearLayout cartContainer;
    private int userId = 1; // TODO: pass logged-in userId dynamically

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        cartContainer = findViewById(R.id.cartContainer);
        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        loadCartItems(userId);
    }

    private void loadCartItems(int userId) {
        Cursor cartCursor = db.rawQuery("SELECT cart_id FROM cart WHERE user_id=?",
                new String[]{String.valueOf(userId)});

        if (cartCursor.moveToFirst()) {
            int cartId = cartCursor.getInt(0);

            Cursor c = db.rawQuery("SELECT ci.cart_item_id, m.name, m.price, ci.quantity, ci.checked " +
                            "FROM cart_items ci " +
                            "JOIN menu_items m ON ci.item_id = m.item_id " +
                            "WHERE ci.cart_id=?",
                    new String[]{String.valueOf(cartId)});

            LayoutInflater inflater = LayoutInflater.from(this);

            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                double price = c.getDouble(2);
                int qty = c.getInt(3);
                boolean checked = c.getInt(4) == 1;

                // Inflate cart_item.xml
                LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.cart_item, cartContainer, false);

                TextView tvName = itemView.findViewById(R.id.tvItemName);
                TextView tvPrice = itemView.findViewById(R.id.tvItemPrice);
                TextView tvQty = itemView.findViewById(R.id.tvItemQty);
                CheckBox cb = itemView.findViewById(R.id.cbSelect);

                tvName.setText(name);
                tvPrice.setText("Rs. " + price);
                tvQty.setText("Qty: " + qty);
                cb.setChecked(checked);

                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    db.execSQL("UPDATE cart_items SET checked=? WHERE cart_item_id=?",
                            new Object[]{isChecked ? 1 : 0, id});
                });

                cartContainer.addView(itemView);
            }
            c.close();
        }
        cartCursor.close();
    }
}
