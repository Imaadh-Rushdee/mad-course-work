package com.example.pizza_mania_app.deliveryPartner;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pizza_mania_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ongoingOrderMaps extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private GoogleMap mMap;
    private TextView statusText;
    private ImageButton readyButton, completedButton;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private LatLng customerLocation;
    private Marker driverMarker;

    private boolean hasArrivedAtCustomer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_order_maps);

        // Receive customer location properly from Intent
        double customerLat = getIntent().getDoubleExtra("customerLat", 0.0);
        double customerLng = getIntent().getDoubleExtra("customerLng", 0.0);
        customerLocation = new LatLng(customerLat, customerLng);

        statusText = findViewById(R.id.statusText);
        readyButton = findViewById(R.id.readyButton);
        completedButton = findViewById(R.id.completedButton);

        completedButton.setEnabled(false);
        statusText.setText("Preparing for delivery...");

        readyButton.setOnClickListener(v -> openGoogleMaps());
        completedButton.setOnClickListener(v -> completeOrder());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Add customer marker
        mMap.addMarker(new MarkerOptions()
                .position(customerLocation)
                .title("Customer Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 14));

        if (checkLocationPermission()) {
            startLocationTracking();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationTracking();
        } else {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startLocationTracking() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000
        ).build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateDriverLocation(location);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    private void updateDriverLocation(Location location) {
        LatLng driverLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (driverMarker == null) {
            driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(driverLatLng)
                    .title("You"));
        } else {
            driverMarker.setPosition(driverLatLng);
        }

        // Zoom to fit driver and customer
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(driverLatLng);
        builder.include(customerLocation);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));

        // Enable complete button if driver is within 100 meters
        float[] results = new float[1];
        Location.distanceBetween(driverLatLng.latitude, driverLatLng.longitude,
                customerLocation.latitude, customerLocation.longitude, results);

        if (!hasArrivedAtCustomer && results[0] <= 100f) { // 100 meters
            hasArrivedAtCustomer = true;
            completedButton.setEnabled(true);
            statusText.setText("Arrived at customer!");
        }
    }

    private void openGoogleMaps() {
        if (customerLocation == null) {
            Toast.makeText(this, "No destination found", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1"
                + "&destination=" + customerLocation.latitude + "," + customerLocation.longitude
                + "&travelmode=driving");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void completeOrder() {
        if (!hasArrivedAtCustomer) {
            Toast.makeText(this, "You must be near the customer", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Order completed!");
        Toast.makeText(this, "Order completed!", Toast.LENGTH_SHORT).show();

        // Go to orderPayment activity
        Intent intent = new Intent(this, orderPayment.class);
        // Pass the orderId (make sure orderId is sent from previous activity)
        int orderId = getIntent().getIntExtra("orderId", -1);
        intent.putExtra("orderId", orderId);
        startActivity(intent);

        finish(); // Close ongoingOrderMaps activity
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
