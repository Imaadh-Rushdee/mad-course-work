package com.example.pizza_mania_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class orderDetails extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng customerLocation = new LatLng(6.9271, 79.8612); // Example: Colombo

    private TextView txtOrderId, txtCustomerName, txtAddress, txtTotalPrice, txtDistanceTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // Initialize UI
        txtOrderId = findViewById(R.id.txtOrderId);
        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtAddress = findViewById(R.id.txtAddress);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        txtDistanceTime = findViewById(R.id.txtDistanceTime);

        // Get data from intent
        String orderId = getIntent().getStringExtra("orderId");
        String customerName = getIntent().getStringExtra("customerName");
        String address = getIntent().getStringExtra("address");
        String totalPrice = getIntent().getStringExtra("totalPrice");

        // Set dummy data in TextViews
        txtOrderId.setText("Order ID: " + orderId);
        txtCustomerName.setText("Customer: " + customerName);
        txtAddress.setText("Address: " + address);
        txtTotalPrice.setText("Total: $" + totalPrice);

        // Map setup
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.addMarker(new MarkerOptions()
                        .position(currentLocation)
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                mMap.addMarker(new MarkerOptions()
                        .position(customerLocation)
                        .title("Customer Location"));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));

                drawRoute(currentLocation, customerLocation);
            }
        });
    }

    private void drawRoute(LatLng origin, LatLng destination) {
        new Thread(() -> {
            try {
                String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                        + origin.latitude + "," + origin.longitude
                        + "&destination=" + destination.latitude + "," + destination.longitude
                        + "&key=YOUR_GOOGLE_MAPS_API_KEY";

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray routes = json.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
                    String distance = leg.getJSONObject("distance").getString("text");
                    String duration = leg.getJSONObject("duration").getString("text");
                    String polyline = route.getJSONObject("overview_polyline").getString("points");

                    runOnUiThread(() -> {
                        txtDistanceTime.setText("Distance: " + distance + ", ETA: " + duration);
                        mMap.addPolyline(new PolylineOptions().addAll(PolyUtil.decode(polyline)).width(8).color(0xFF2196F3));
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
