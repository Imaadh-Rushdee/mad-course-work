package com.example.pizza_mania_app.deliveryPartner;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania_app.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ongoingOrder extends AppCompatActivity implements OnMapReadyCallback {

    private int orderId;
    private SQLiteDatabase db;
    private GoogleMap mMap;
    private ImageView statusPreparingIcon;
    private ImageView statusReadyIcon;
    private ImageView statusCompletedIcon;
    private Button completeOrderButton;
    private Handler handler = new Handler();
    private Runnable statusChecker;
    private double customerLat;
    private double customerLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ongoing_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }

        orderId = getIntent().getIntExtra("orderId", -1);

        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        statusPreparingIcon = findViewById(R.id.statusPreparingIcon);
        statusReadyIcon = findViewById(R.id.statusReadyIcon);
        statusCompletedIcon = findViewById(R.id.statusCompletedIcon);
        completeOrderButton = findViewById(R.id.completeOrder);
        completeOrderButton.setEnabled(false);

        startStatusListener();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadOrderLocation(); // show marker on map
    }

    private void loadOrderLocation() {
        Cursor cursor = db.rawQuery("SELECT address_latitude, address_longitude, order_address FROM orders WHERE order_id=?",
                new String[]{String.valueOf(orderId)});
        if(cursor.moveToFirst()) {
            customerLat = cursor.getDouble(cursor.getColumnIndexOrThrow("address_latitude"));
            customerLng = cursor.getDouble(cursor.getColumnIndexOrThrow("address_longitude"));
            String address = cursor.getString(cursor.getColumnIndexOrThrow("order_address"));

            LatLng location = new LatLng(customerLat, customerLng);
            mMap.addMarker(new MarkerOptions().position(location).title("Delivery Address").snippet(address));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
        cursor.close();
    }
    private void startStatusListener() {
        statusChecker = new Runnable() {
            @Override
            public void run() {
                checkOrderStatus();
                handler.postDelayed(this, 3000); // repeat every 3 seconds
            }
        };
        handler.post(statusChecker);
    }
    private void stopStatusListener() {
        handler.removeCallbacks(statusChecker);
    }

    private void checkOrderStatus() {
        Cursor cursor = db.rawQuery("SELECT order_status FROM orders WHERE order_id=?", new String[]{String.valueOf(orderId)});
        if(cursor.moveToFirst()) {
            String status = cursor.getString(cursor.getColumnIndexOrThrow("order_status"));
            updateStatusUI(status);
        }
        cursor.close();
    }

    private void updateStatusUI(String status) {
        switch (status) {
            case "preparing":
                statusPreparingIcon.setImageResource(R.drawable.ic_circle_green);
                statusReadyIcon.setImageResource(R.drawable.ic_circle_grey);
                statusCompletedIcon.setImageResource(R.drawable.ic_circle_grey);
                break;
            case "ready":
                statusPreparingIcon.setImageResource(R.drawable.ic_circle_green);
                statusReadyIcon.setImageResource(R.drawable.ic_circle_green);
                statusCompletedIcon.setImageResource(R.drawable.ic_circle_grey);

                startNavigationToCustomer();
                break;
            case "delivered":
                statusPreparingIcon.setImageResource(R.drawable.ic_circle_green);
                statusReadyIcon.setImageResource(R.drawable.ic_circle_green);
                statusCompletedIcon.setImageResource(R.drawable.ic_circle_green);

                completeOrderButton.setEnabled(true);

                stopStatusListener();
                break;
        }
    }

    private void startNavigationToCustomer() {
        String uri = "google.navigation:q=" + customerLat + "," + customerLng + "&mode=d";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);

        Toast.makeText(this, "Navigation started!", Toast.LENGTH_SHORT).show();
    }

    public void completeOrder(View view) {
        Toast.makeText(this, "Order marked as completed!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, orderPayment.class);
        intent.putExtra("orderId", orderId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopStatusListener();
    }
}
