package com.example.pizza_mania_app.deliveryPartner;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.example.pizza_mania_app.R;
import com.example.pizza_mania_app.databinding.ActivityOngoingOrderMapsBinding;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ongoingOrderMaps extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String DIRECTIONS_API_KEY = "AIzaSyCldldEy5A5sk7K3-RkyHhoCH86XeToP8s"; // Replace with your API key

    // UI Components
    private GoogleMap mMap;
    private ActivityOngoingOrderMapsBinding binding;

    // Location Services
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // Data
    private SQLiteDatabase database;
    private int orderId;

    // Map Elements
    private LatLng customerLocation;
    private LatLng currentDriverLocation;
    private Marker driverMarker;
    private Marker customerMarker;
    private boolean isRouteDrawn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize binding
        binding = ActivityOngoingOrderMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get data from intent
        orderId = getIntent().getIntExtra("orderId", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Invalid order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize components
        initializeComponents();
        setupMap();
    }

    private void initializeComponents() {
        // Initialize database
        database = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                handleLocationUpdate(locationResult.getLastLocation());
            }
        };
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Check permissions and start navigation
        if (checkLocationPermission()) {
            startNavigation();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startNavigation();
            } else {
                Toast.makeText(this, "Location permission is required for navigation", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startNavigation() {
        loadCustomerLocation();
        startLocationTracking();
    }

    private void loadCustomerLocation() {
        try {
            Cursor cursor = database.rawQuery(
                    "SELECT address_latitude, address_longitude FROM orders WHERE order_id = ?",
                    new String[]{String.valueOf(orderId)}
            );

            if (cursor.moveToFirst()) {
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("address_latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("address_longitude"));

                customerLocation = new LatLng(latitude, longitude);
                addCustomerMarker();
            } else {
                Toast.makeText(this, "Customer location not found", Toast.LENGTH_SHORT).show();
                finish();
            }
            cursor.close();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading customer location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void addCustomerMarker() {
        if (customerLocation != null && mMap != null) {
            customerMarker = mMap.addMarker(new MarkerOptions()
                    .position(customerLocation)
                    .title("Customer Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    private void startLocationTracking() {
        if (!checkLocationPermission()) return;

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateDelayMillis(10000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void handleLocationUpdate(Location location) {
        if (location == null) return;

        currentDriverLocation = new LatLng(location.getLatitude(), location.getLongitude());

        // Update or create driver marker
        updateDriverMarker();

        // Draw route only once
        if (!isRouteDrawn && customerLocation != null) {
            drawRouteToCustomer();
            isRouteDrawn = true;
        }

        // Adjust camera to show both markers
        adjustCameraView();
    }

    private void updateDriverMarker() {
        if (currentDriverLocation == null || mMap == null) return;

        if (driverMarker == null) {
            // Create new driver marker
            driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentDriverLocation)
                    .title("Your Location (Driver)")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        } else {
            // Update existing marker position
            driverMarker.setPosition(currentDriverLocation);
        }
    }

    private void adjustCameraView() {
        if (currentDriverLocation == null || customerLocation == null || mMap == null) return;

        // Create bounds that include both driver and customer locations
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(currentDriverLocation);
        boundsBuilder.include(customerLocation);

        LatLngBounds bounds = boundsBuilder.build();

        // Animate camera to show both locations
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    private void drawRouteToCustomer() {
        if (currentDriverLocation == null || customerLocation == null) return;

        // Run network operation in background thread
        new Thread(() -> {
            try {
                String directionsUrl = buildDirectionsUrl(currentDriverLocation, customerLocation);
                String response = makeHttpRequest(directionsUrl);
                List<LatLng> routePoints = parseDirectionsResponse(response);

                // Update UI on main thread
                runOnUiThread(() -> drawRoute(routePoints));

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error drawing route: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
                e.printStackTrace();
            }
        }).start();
    }

    private String buildDirectionsUrl(LatLng origin, LatLng destination) {
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&mode=driving" +
                "&key=" + DIRECTIONS_API_KEY;
    }

    private String makeHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        return response.toString();
    }

    private List<LatLng> parseDirectionsResponse(String jsonResponse) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray routes = jsonObject.getJSONArray("routes");

        if (routes.length() == 0) {
            throw new Exception("No routes found");
        }

        JSONObject route = routes.getJSONObject(0);
        JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
        String encodedPoints = overviewPolyline.getString("points");

        return decodePolyline(encodedPoints);
    }

    private void drawRoute(List<LatLng> routePoints) {
        if (routePoints == null || routePoints.isEmpty() || mMap == null) return;

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(8)
                .color(Color.BLUE)
                .geodesic(true);

        mMap.addPolyline(polylineOptions);
    }

    private List<LatLng> decodePolyline(String encodedPath) {
        List<LatLng> polylinePoints = new ArrayList<>();
        int index = 0, len = encodedPath.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encodedPath.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int deltaLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += deltaLat;

            shift = 0;
            result = 0;
            do {
                b = encodedPath.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int deltaLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += deltaLng;

            polylinePoints.add(new LatLng((double) lat / 1E5, (double) lng / 1E5));
        }
        return polylinePoints;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop location updates
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        // Close database
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
}