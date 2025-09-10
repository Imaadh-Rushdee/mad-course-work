package com.example.pizza_mania_app.deliveryPartner;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania_app.R;
import com.example.pizza_mania_app.helperClasses.deliveryOrders;
import com.example.pizza_mania_app.profile;

import java.util.ArrayList;

public class deliveryPartnerDashboard extends AppCompatActivity {

    ArrayList<deliveryOrders> orders = new ArrayList<>();
    private SQLiteDatabase db;
    private int partnerId;
    private int partnerBranchId;
    private TextView partnerName;

    private ImageView profileImage;   // For showing the photo
    private Spinner profileSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_partner_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        partnerName = findViewById(R.id.driver_name);
        profileImage = findViewById(R.id.profileImage);    // For displaying photo
        profileSpinner = findViewById(R.id.profileSpinner); // For menu dropdown

        // Simple spinner setup
        String[] options = {"Profile", "Logout"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_layout, options);
        profileSpinner.setAdapter(adapter);

        profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Profile - do whatever you want
                    Toast.makeText(deliveryPartnerDashboard.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(deliveryPartnerDashboard.this, profile.class);
                    intent.putExtra("userId", partnerId);
                    startActivity(intent);

                } else if (position == 1) {
                    // Logout - go back to login
                    finish();
                }
                profileSpinner.setSelection(0); // Reset to "Menu"
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        partnerId = getIntent().getIntExtra("partnerId", -1);
        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);
        setPartnerData(partnerId);
        setOrderData();
    }
    private void setPartnerData(int partnerId) {
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE user_id=?", new String[]{String.valueOf(partnerId)});
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));

            partnerName.setText(name);
            partnerBranchId = cursor.getInt(cursor.getColumnIndexOrThrow("branch_id"));

            int picIndex = cursor.getColumnIndex("profile_pic"); // safer than getColumnIndexOrThrow
            if (picIndex != -1) {
                String profilePicUrl = cursor.getString(picIndex);
                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(profilePicUrl);
                    if (bitmap != null) {
                        profileImage.setImageBitmap(bitmap);
                    } else {
                        profileImage.setImageResource(R.drawable.profile);
                    }
                } else {
                    profileImage.setImageResource(R.drawable.profile);
                }
            } else {
                profileImage.setImageResource(R.drawable.profile); // fallback
            }
        }
        cursor.close();
    }
    public void refreshButton(View view){
        setOrderData();
    }
    private void setOrderData() {
        LinearLayout container = findViewById(R.id.ordersContainer);
        container.removeAllViews();

        // Fix column name and query
        Cursor cursor = db.rawQuery("SELECT * FROM orders WHERE order_status=? AND order_type=? AND branch_id=?", new String[]{"ready","delivery",String.valueOf(partnerBranchId)});// Example: only pending deliveries

        while (cursor.moveToNext()) {
            int orderId = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
            String customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name"));
            String deliveryAddress = cursor.getString(cursor.getColumnIndexOrThrow("order_address"));
            double orderTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));

            LinearLayout card = (LinearLayout) getLayoutInflater().inflate(R.layout.order_card, container, false);
            TextView tvOrderId = card.findViewById(R.id.cardOrderId);
            TextView tvCustomerName = card.findViewById(R.id.orderCustomerName);
            TextView tvDeliveryAddress = card.findViewById(R.id.orderCustomerAddress);
            TextView tvOrderTotal = card.findViewById(R.id.orderTotalAmount);

            tvOrderId.setText(String.valueOf(orderId));
            tvCustomerName.setText(customerName);
            tvDeliveryAddress.setText(deliveryAddress);
            tvOrderTotal.setText(String.valueOf(orderTotal));

            card.setOnClickListener(view -> onClickOrder(orderId));
            container.addView(card);
        }
        cursor.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        setOrderData();
    }

    private void onClickOrder(int clickedOrderId) {
        Intent intent = new Intent(this, orderDetailsMaps.class);
        intent.putExtra("orderId", clickedOrderId);
        startActivity(intent);
    }
}
