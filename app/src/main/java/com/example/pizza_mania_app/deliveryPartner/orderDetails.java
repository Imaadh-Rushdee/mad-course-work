package com.example.pizza_mania_app.deliveryPartner;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import com.example.pizza_mania_app.R;

public class orderDetails extends AppCompatActivity implements OnMapReadyCallback{

    private TextView selectedOrderId;
    private TextView selectedCustomerName;
    private TextView selectedOrderCart;
    private TextView selectedOrderAddress;
    private TextView orderTotalAmount;
    private SupportMapFragment supportMapFragment;
    private int orderId;
    private SQLiteDatabase db;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if(supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }

        orderId = getIntent().getIntExtra("orderId", -1);
        db = openOrCreateDatabase("pizza_mania.db",MODE_PRIVATE, null);

        selectedOrderId = findViewById(R.id.orderId);
        selectedCustomerName = findViewById(R.id.customerName);
        selectedOrderCart = findViewById(R.id.orderItem);
        selectedOrderAddress = findViewById(R.id.address);
        orderTotalAmount = findViewById(R.id.totalAmount);

    }

    public void setOrderData() {
        Cursor cursor = db.rawQuery("SELECT * FROM orders WHERE order_id=?",new String[]{String.valueOf(orderId)});
        if(cursor.moveToFirst()) {
            String customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name"));
            String orderCart = cursor.getString(cursor.getColumnIndexOrThrow("order_cart"));
            String orderAddress = cursor.getString(cursor.getColumnIndexOrThrow("order_address"));
            double orderTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("order_total"));
            double mapLongitude = cursor.getDouble(cursor.getColumnIndexOrThrow("address_longitude"));
            double mapLatitude = cursor.getDouble(cursor.getColumnIndexOrThrow("address_latitude"));

            selectedOrderId.setText(String.valueOf(orderId));
            selectedCustomerName.setText(String.valueOf(customerName));
            selectedOrderAddress.setText(String.valueOf(orderAddress));
            selectedOrderCart.setText(String.valueOf(orderCart));
            orderTotalAmount.setText(String.format("Rs.%.2f", orderTotal));
            //set the address to the google map
            LatLng location = new LatLng(mapLatitude, mapLongitude);
            mMap.addMarker(new MarkerOptions().position(location).title("Delivery Address").snippet(orderAddress));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
        cursor.close();
    }
    public void onClickAcceptOrder() {

    }

    public void onClickBack() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setOrderData();
    }
}
