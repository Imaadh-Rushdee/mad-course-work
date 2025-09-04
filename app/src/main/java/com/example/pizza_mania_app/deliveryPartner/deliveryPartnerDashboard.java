package com.example.pizza_mania_app.deliveryPartner;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania_app.R;
import com.example.pizza_mania_app.helperClasses.deliveryOrders;

import java.util.ArrayList;

public class deliveryPartnerDashboard extends AppCompatActivity {

    ArrayList<deliveryOrders> orders = new ArrayList<>();
    private SQLiteDatabase db;
    private int partnerId;
    private TextView partnerName;
    private ImageButton profilePic;

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
        profilePic = findViewById(R.id.profileImg);

        partnerId = getIntent().getIntExtra("partnerId", -1);
        db = openOrCreateDatabase("pizza_mania.db",MODE_PRIVATE, null);
        setPartnerData(partnerId);
        setOrderData();
    }

    public void setPartnerData(int partnerId) {

        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE userId=?", new String[]{String.valueOf(partnerId)});
        if(cursor.moveToFirst()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String phone_number = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String profilePicUrl = cursor.getString(cursor.getColumnIndexOrThrow("profile_pic"));


            partnerName.setText(name);

            if(profilePicUrl != null && !profilePicUrl.isEmpty()) {
                Bitmap bitmap = BitmapFactory.decodeFile(profilePicUrl);
                if(bitmap != null) {
                    profilePic.setImageBitmap(bitmap);
                }
                else {
                    profilePic.setImageResource(R.drawable.profile);
                }
            }
            else {
                profilePic.setImageResource(R.drawable.profile);
            }
        }
        cursor.close();
    }
    public void setOrderData() {

        LinearLayout container = findViewById(R.id.ordersContainer);
        container.removeAllViews();

        Cursor cursor = db.rawQuery("SELECT * FROM orders WHERE order_type=?", new String[]{"delivery"});

        while(cursor.moveToNext()){

            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name"));
            String deliveryAddress = cursor.getString(cursor.getColumnIndexOrThrow("delivery_address"));
            double orderTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));

            LinearLayout card = (LinearLayout) getLayoutInflater().inflate(R.layout.order_card, container, false);
            TextView orderId = card.findViewById(R.id.cardOrderId);
            TextView orderCustomerName = card.findViewById(R.id.orderCustomerName);
            TextView orderDeliveryAddress = card.findViewById(R.id.orderCustomerAddress);
            TextView orderTotalAmount = card.findViewById(R.id.orderTotalAmount);

            orderId.setText(String.valueOf(id));
            orderCustomerName.setText(customerName);
            orderDeliveryAddress.setText(deliveryAddress);
            orderTotalAmount.setText(String.valueOf(orderTotal));
            card.setOnClickListener(view -> onClickOrder(id));

            container.addView(card);
        }


        cursor.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        setOrderData();
    }
    public void onClickOrder(int clickedOrderId) {
        Intent intent = new Intent(this, orderDetails.class);
        intent.putExtra("orderId", clickedOrderId);
        startActivity(intent);
    }
    public void onClickProfile() {

    }

}
