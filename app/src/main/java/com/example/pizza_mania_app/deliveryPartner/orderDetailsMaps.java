package com.example.pizza_mania_app.deliveryPartner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class orderDetailsMaps extends AppCompatActivity implements OnMapReadyCallback {

    private static final String DIRECTIONS_API_KEY = "AIzaSyCldldEy5A5sk7K3-RkyHhoCH86XeToP8s";
    private static final int LOCATION_PERMISSION_REQUEST = 101;

    // UI Components
    private GoogleMap mMap;
    private TextView txtDistanceTime;
    private Button acceptBtn;

    // Location Services
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // Map Elements
    private Marker driverMarker, customerMarker;
    private Polyline routePolyline;
    private LatLng customerLocation, driverLocation;

    // Data
    private int orderId;
    private boolean routeRequested = false;

    // Performance optimization
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_maps);

        initializeComponents();
        setupMap();
        setupClickListeners();
    }

    private void initializeComponents() {
        // Initialize UI elements
        txtDistanceTime = findViewById(R.id.txtDistanceTime);
        acceptBtn = findViewById(R.id.acceptOrder);
        orderId = getIntent().getIntExtra("orderId", -1);

        // Initialize services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Set initial state
        txtDistanceTime.setText("Calculating route...");
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupClickListeners() {
        acceptBtn.setOnClickListener(v -> {
            acceptOrder();
        });
    }

    private void acceptOrder() {
        executorService.execute(() -> {
            try {
                SQLiteDatabase db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);
                db.execSQL("UPDATE orders SET order_status='ongoing' WHERE order_id=?",
                        new Object[]{orderId});
                db.close();

                mainHandler.post(() -> {
                    Intent intent = new Intent(this, ongoingOrderMaps.class);
                    intent.putExtra("orderId", orderId);
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "Error accepting order", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map for better performance
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // Disable to improve performance

        if (checkLocationPermission()) {
            startLocationAndRoute();
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

    private void startLocationAndRoute() {
        loadCustomerLocation();
        startLocationUpdates();
    }

    private void loadCustomerLocation() {
        executorService.execute(() -> {
            try {
                SQLiteDatabase db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);
                Cursor cursor = db.rawQuery(
                        "SELECT address_latitude, address_longitude FROM orders WHERE order_id=?",
                        new String[]{String.valueOf(orderId)}
                );

                if (cursor.moveToFirst()) {
                    double lat = cursor.getDouble(0);
                    double lng = cursor.getDouble(1);
                    customerLocation = new LatLng(lat, lng);

                    mainHandler.post(() -> addCustomerMarker());
                }
                cursor.close();
                db.close();
            } catch (Exception e) {
                Log.e("OrderMaps", "Error loading customer location", e);
                mainHandler.post(() ->
                        Toast.makeText(this, "Error loading customer location", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void addCustomerMarker() {
        if (customerLocation != null && mMap != null) {
            customerMarker = mMap.addMarker(new MarkerOptions()
                    .position(customerLocation)
                    .title("Customer Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    private void startLocationUpdates() {
        if (!checkLocationPermission()) return;

        // Optimize location request for faster updates
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    handleLocationUpdate(location);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void handleLocationUpdate(Location location) {
        driverLocation = new LatLng(location.getLatitude(), location.getLongitude());

        updateDriverMarker();

        // Only request route once when we have both locations
        if (customerLocation != null && !routeRequested) {
            routeRequested = true;
            calculateRouteOptimized();
        }

        adjustCameraView();
    }

    private void updateDriverMarker() {
        if (driverLocation == null || mMap == null) return;

        if (driverMarker == null) {
            driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(driverLocation)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        } else {
            driverMarker.setPosition(driverLocation);
        }
    }

    private void adjustCameraView() {
        if (driverLocation == null || customerLocation == null || mMap == null) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(driverLocation);
        builder.include(customerLocation);
        LatLngBounds bounds = builder.build();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    private void calculateRouteOptimized() {
        if (driverLocation == null || customerLocation == null) return;

        // Show straight-line distance immediately for instant feedback
        showStraightLineDistance();

        // Get actual route in background
        executorService.execute(() -> {
            try {
                String directionsUrl = buildDirectionsUrl();
                String response = makeHttpRequest(directionsUrl);
                RouteData routeData = parseRouteResponse(response);

                if (routeData != null) {
                    mainHandler.post(() -> displayRoute(routeData));
                } else {
                    mainHandler.post(() ->
                            txtDistanceTime.setText("Route calculation failed")
                    );
                }
            } catch (Exception e) {
                Log.e("OrderMaps", "Error calculating route", e);
                mainHandler.post(() ->
                        txtDistanceTime.setText("Error calculating route")
                );
            }
        });
    }

    private void showStraightLineDistance() {
        float[] results = new float[1];
        Location.distanceBetween(
                driverLocation.latitude, driverLocation.longitude,
                customerLocation.latitude, customerLocation.longitude,
                results
        );

        float distanceKm = results[0] / 1000;
        txtDistanceTime.setText(String.format("~%.1f km (calculating route...)", distanceKm));
    }

    private String buildDirectionsUrl() {
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + driverLocation.latitude + "," + driverLocation.longitude +
                "&destination=" + customerLocation.latitude + "," + customerLocation.longitude +
                "&mode=driving" +
                "&key=" + DIRECTIONS_API_KEY;
    }

    private String makeHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(8000); // Reduced timeout for faster failure
        connection.setReadTimeout(8000);

        int responseCode = connection.getResponseCode();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                responseCode == 200 ? connection.getInputStream() : connection.getErrorStream()
        ));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        if (responseCode != 200) {
            throw new Exception("HTTP " + responseCode + ": " + response.toString());
        }

        return response.toString();
    }

    private RouteData parseRouteResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            String status = json.getString("status");

            if (!"OK".equals(status)) {
                String errorMessage = json.optString("error_message", status);
                Log.e("OrderMaps", "API Error: " + errorMessage);
                return null;
            }

            JSONArray routes = json.getJSONArray("routes");
            if (routes.length() == 0) return null;

            JSONObject route = routes.getJSONObject(0);
            JSONObject leg = route.getJSONArray("legs").getJSONObject(0);

            String distance = leg.getJSONObject("distance").getString("text");
            String duration = leg.getJSONObject("duration").getString("text");
            String polylinePoints = route.getJSONObject("overview_polyline").getString("points");

            List<LatLng> routePoints = decodePolyline(polylinePoints);

            return new RouteData(distance, duration, routePoints);

        } catch (Exception e) {
            Log.e("OrderMaps", "Error parsing route response", e);
            return null;
        }
    }

    private void displayRoute(RouteData routeData) {
        // Update distance and time
        txtDistanceTime.setText("Distance: " + routeData.distance + " | ETA: " + routeData.duration);

        // Draw route on map
        drawRoute(routeData.routePoints);
    }

    private void drawRoute(List<LatLng> routePoints) {
        if (routePoints == null || routePoints.isEmpty() || mMap == null) return;

        // Remove existing route
        if (routePolyline != null) {
            routePolyline.remove();
        }

        // Draw new route
        routePolyline = mMap.addPolyline(new PolylineOptions()
                .addAll(routePoints)
                .width(8)
                .color(Color.BLUE)
                .geodesic(true));
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    startLocationAndRoute();
                }
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up location updates
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        // Clean up executor service
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    // Helper class to hold route data
    private static class RouteData {
        final String distance;
        final String duration;
        final List<LatLng> routePoints;

        RouteData(String distance, String duration, List<LatLng> routePoints) {
            this.distance = distance;
            this.duration = duration;
            this.routePoints = routePoints;
        }
    }
}