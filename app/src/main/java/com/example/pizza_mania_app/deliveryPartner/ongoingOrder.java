package com.example.pizza_mania_app.deliveryPartner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.pizza_mania_app.R;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ongoingOrder extends AppCompatActivity implements OnMapReadyCallback {

    private static final String DIRECTIONS_API_KEY = "YOUR_API_KEY_HERE"; // add your key

    private int orderId;
    private SQLiteDatabase db;
    private GoogleMap mMap;
    private ImageView statusPreparingIcon, statusReadyIcon, statusCompletedIcon;
    private Button completeOrderButton;
    private Handler handler = new Handler();
    private Runnable statusChecker;

    private double customerLat, customerLng;
    private FusedLocationProviderClient fusedLocation;
    private Marker driverMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_order);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        orderId = getIntent().getIntExtra("orderId", -1);
        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        statusPreparingIcon = findViewById(R.id.statusPreparingIcon);
        statusReadyIcon = findViewById(R.id.statusReadyIcon);
        statusCompletedIcon = findViewById(R.id.statusCompletedIcon);
        completeOrderButton = findViewById(R.id.completeOrder);
        completeOrderButton.setEnabled(false);

        fusedLocation = LocationServices.getFusedLocationProviderClient(this);

        startStatusListener();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadCustomerLocation();
    }

    private void loadCustomerLocation() {
        Cursor cursor = db.rawQuery("SELECT address_latitude, address_longitude, order_address FROM orders WHERE order_id=?",
                new String[]{String.valueOf(orderId)});
        if (cursor.moveToFirst()) {
            customerLat = cursor.getDouble(cursor.getColumnIndexOrThrow("address_latitude"));
            customerLng = cursor.getDouble(cursor.getColumnIndexOrThrow("address_longitude"));
            String address = cursor.getString(cursor.getColumnIndexOrThrow("order_address"));

            LatLng customerLatLng = new LatLng(customerLat, customerLng);
            mMap.addMarker(new MarkerOptions().position(customerLatLng).title("Delivery Address").snippet(address));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLatLng, 15));
        }
        cursor.close();

        startDriverLocationUpdates();
    }

    private void startDriverLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        LocationRequest request = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(4000)
                .setFastestInterval(2000);

        fusedLocation.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result != null && result.getLastLocation() != null) {
                    Location loc = result.getLastLocation();
                    LatLng driverLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());

                    if (driverMarker == null) {
                        driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver"));
                    } else {
                        driverMarker.setPosition(driverLatLng);
                    }

                    drawRoute(driverLatLng, new LatLng(customerLat, customerLng));
                }
            }
        }, Looper.getMainLooper());
    }

    private void drawRoute(LatLng driver, LatLng customer) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(driver).title("Driver"));
        mMap.addMarker(new MarkerOptions().position(customer).title("Customer"));

        LatLngBounds bounds = new LatLngBounds.Builder().include(driver).include(customer).build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

        new Thread(() -> {
            try {
                String urlStr = "https://maps.googleapis.com/maps/api/directions/json?origin="
                        + driver.latitude + "," + driver.longitude
                        + "&destination=" + customer.latitude + "," + customer.longitude
                        + "&mode=driving&key=" + DIRECTIONS_API_KEY;

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray routes = json.getJSONArray("routes");
                if (routes.length() == 0) return;

                JSONObject leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0);

                final ArrayList<LatLng> points = decodePolyline(
                        routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
                );

                runOnUiThread(() -> mMap.addPolyline(new PolylineOptions().addAll(points).width(10).color(Color.BLUE)));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private ArrayList<LatLng> decodePolyline(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, lat = 0, lng = 0;
        while (index < encoded.length()) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return poly;
    }

    private void startStatusListener() {
        statusChecker = new Runnable() {
            @Override
            public void run() {
                checkOrderStatus();
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(statusChecker);
    }

    private void stopStatusListener() {
        handler.removeCallbacks(statusChecker);
    }

    private void checkOrderStatus() {
        Cursor cursor = db.rawQuery("SELECT order_status FROM orders WHERE order_id=?",
                new String[]{String.valueOf(orderId)});
        if (cursor.moveToFirst()) {
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

    public void completeOrder(View view) {
        Toast.makeText(this, "Order marked as completed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopStatusListener();
    }
}
