package com.example.pizza_mania_app.deliveryPartner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
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

public class orderDetails extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQ_LOCATION = 100;
    // ⚠️ replace with your valid unrestricted Directions API key
    private static final String DIRECTIONS_API_KEY = "YOUR_API_KEY_HERE";

    private TextView txtDistanceTime;
    private int orderId;
    private SQLiteDatabase db;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocation;
    private LatLng customerLatLng;
    private Marker driverMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        txtDistanceTime = findViewById(R.id.txtDistanceTime);
        orderId = getIntent().getIntExtra("orderId", -1);
        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        fusedLocation = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
        }

        loadOrderAndShow();
    }

    private void loadOrderAndShow() {
        if (orderId < 0) return;

        Cursor c = db.rawQuery(
                "SELECT order_address, address_latitude, address_longitude FROM orders WHERE order_id=?",
                new String[]{String.valueOf(orderId)}
        );

        if (!c.moveToFirst()) {
            c.close();
            Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat = c.getDouble(c.getColumnIndexOrThrow("address_latitude"));
        double lng = c.getDouble(c.getColumnIndexOrThrow("address_longitude"));
        c.close();

        customerLatLng = new LatLng(lat, lng);

        // Customer marker
        mMap.addMarker(new MarkerOptions().position(customerLatLng).title("Customer"));

        // Start fetching driver location continuously
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startDriverLocationUpdates();
        }
    }

    private void startDriverLocationUpdates() {
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

                    // Update or add driver marker
                    if (driverMarker == null) {
                        driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver"));
                    } else {
                        driverMarker.setPosition(driverLatLng);
                    }

                    // Draw route each time driver moves
                    drawRoute(driverLatLng, customerLatLng);
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

                Log.d("DirectionsAPI", "Response: " + sb);

                JSONObject json = new JSONObject(sb.toString());
                JSONArray routes = json.getJSONArray("routes");
                if (routes.length() == 0) {
                    runOnUiThread(() -> txtDistanceTime.setText("No route found"));
                    return;
                }

                JSONObject leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0);
                final String distance = leg.getJSONObject("distance").getString("text");
                final String duration = leg.getJSONObject("duration").getString("text");

                final ArrayList<LatLng> points = decodePolyline(
                        routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
                );

                runOnUiThread(() -> {
                    mMap.addPolyline(new PolylineOptions().addAll(points).width(10).color(Color.BLUE));
                    txtDistanceTime.setText(distance + " | " + duration);
                });

            } catch (Exception e) {
                runOnUiThread(() -> txtDistanceTime.setText("Route unavailable"));
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadOrderAndShow();
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickAcceptOrder(android.view.View v) {
        try {
            db.execSQL("UPDATE orders SET order_status='accepted' WHERE order_id=?", new Object[]{orderId});
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this, ongoingOrder.class).putExtra("orderId", orderId));
    }

    public void onClickBack(android.view.View v) {
        finish();
    }
}
