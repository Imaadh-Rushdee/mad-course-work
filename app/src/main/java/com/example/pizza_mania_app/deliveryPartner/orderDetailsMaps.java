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

    private GoogleMap mMap;
    private TextView txtDistanceTime;
    private Button acceptBtn;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker driverMarker, customerMarker;
    private Polyline routePolyline;
    private LatLng customerLocation, driverLocation;
    private int orderId;
    private boolean routeRequested = false;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_maps);

        txtDistanceTime = findViewById(R.id.txtDistanceTime);
        acceptBtn = findViewById(R.id.acceptOrder);
        orderId = getIntent().getIntExtra("orderId", -1);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        txtDistanceTime.setText("Calculating route...");

        setupMap();
        acceptBtn.setOnClickListener(v -> acceptOrder());
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void acceptOrder() {
        if (!checkLocationPermission()) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double driverLat = location.getLatitude();
                double driverLng = location.getLongitude();

                executorService.execute(() -> {
                    try {
                        SQLiteDatabase db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);
                        db.execSQL("UPDATE orders SET order_status='ongoing' WHERE order_id=?", new Object[]{orderId});
                        db.close();

                        mainHandler.post(() -> {
                            Intent intent = new Intent(this, ongoingOrderMaps.class);
                            intent.putExtra("orderId", orderId);
                            intent.putExtra("driverLat", driverLat);
                            intent.putExtra("driverLng", driverLng);
                            intent.putExtra("customerLat", customerLocation.latitude);
                            intent.putExtra("customerLng", customerLocation.longitude);
                            startActivity(intent);
                            finish();
                        });
                    } catch (Exception e) {
                        mainHandler.post(() -> Toast.makeText(this, "Error accepting order", Toast.LENGTH_SHORT).show());
                    }
                });

            } else {
                Toast.makeText(this, "Unable to get driver location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (checkLocationPermission()) startLocationAndRoute();
        else requestLocationPermission();
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
    }

    private void startLocationAndRoute() {
        loadCustomerLocation();
        startLocationUpdates();
    }

    private void loadCustomerLocation() {
        executorService.execute(() -> {
            try {
                SQLiteDatabase db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);
                Cursor cursor = db.rawQuery("SELECT address_latitude, address_longitude FROM orders WHERE order_id=?", new String[]{String.valueOf(orderId)});
                if (cursor.moveToFirst()) {
                    customerLocation = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
                    mainHandler.post(this::addCustomerMarker);
                }
                cursor.close();
                db.close();
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "Error loading customer location", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void addCustomerMarker() {
        if (customerLocation != null && mMap != null) {
            customerMarker = mMap.addMarker(new MarkerOptions().position(customerLocation).title("Customer Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    private void startLocationUpdates() {
        if (!checkLocationPermission()) return;

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setWaitForAccurateLocation(false).setMinUpdateIntervalMillis(2000).setMaxUpdateDelayMillis(5000).build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) handleLocationUpdate(location);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void handleLocationUpdate(Location location) {
        driverLocation = new LatLng(location.getLatitude(), location.getLongitude());
        updateDriverMarker();
        if (customerLocation != null && !routeRequested) {
            routeRequested = true;
            calculateRouteOptimized();
        }
        adjustCameraView();
    }

    private void updateDriverMarker() {
        if (driverLocation == null || mMap == null) return;
        if (driverMarker == null) {
            driverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        } else driverMarker.setPosition(driverLocation);
    }

    private void adjustCameraView() {
        if (driverLocation == null || customerLocation == null || mMap == null) return;
        LatLngBounds bounds = new LatLngBounds.Builder().include(driverLocation).include(customerLocation).build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    private void calculateRouteOptimized() {
        if (driverLocation == null || customerLocation == null) return;
        showStraightLineDistance();

        executorService.execute(() -> {
            try {
                String response = makeHttpRequest(buildDirectionsUrl());
                RouteData routeData = parseRouteResponse(response);
                if (routeData != null) mainHandler.post(() -> displayRoute(routeData));
                else mainHandler.post(() -> txtDistanceTime.setText("Route calculation failed"));
            } catch (Exception e) {
                mainHandler.post(() -> txtDistanceTime.setText("Error calculating route"));
            }
        });
    }

    private void showStraightLineDistance() {
        float[] results = new float[1];
        Location.distanceBetween(driverLocation.latitude, driverLocation.longitude, customerLocation.latitude, customerLocation.longitude, results);
        txtDistanceTime.setText(String.format("~%.1f km (calculating route...)", results[0] / 1000));
    }

    private String buildDirectionsUrl() {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" + driverLocation.latitude + "," + driverLocation.longitude +
                "&destination=" + customerLocation.latitude + "," + customerLocation.longitude +
                "&mode=driving&key=" + DIRECTIONS_API_KEY;
    }

    private String makeHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        int responseCode = connection.getResponseCode();

        BufferedReader reader = new BufferedReader(new InputStreamReader(responseCode == 200 ? connection.getInputStream() : connection.getErrorStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();
        connection.disconnect();
        if (responseCode != 200) throw new Exception("HTTP " + responseCode + ": " + response.toString());
        return response.toString();
    }

    private RouteData parseRouteResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            if (!"OK".equals(json.getString("status"))) return null;
            JSONObject leg = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);
            String distance = leg.getJSONObject("distance").getString("text");
            String duration = leg.getJSONObject("duration").getString("text");
            String polylinePoints = json.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
            return new RouteData(distance, duration, decodePolyline(polylinePoints));
        } catch (Exception e) {
            return null;
        }
    }

    private void displayRoute(RouteData routeData) {
        txtDistanceTime.setText("Distance: " + routeData.distance + " | ETA: " + routeData.duration);
        drawRoute(routeData.routePoints);
    }

    private void drawRoute(List<LatLng> routePoints) {
        if (routePoints == null || routePoints.isEmpty() || mMap == null) return;
        if (routePolyline != null) routePolyline.remove();
        routePolyline = mMap.addPolyline(new PolylineOptions().addAll(routePoints).width(8).color(Color.BLUE).geodesic(true));
    }

    private List<LatLng> decodePolyline(String encodedPath) {
        List<LatLng> polylinePoints = new ArrayList<>();
        int index = 0, lat = 0, lng = 0;
        while (index < encodedPath.length()) {
            int b, shift = 0, result = 0;
            do { b = encodedPath.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
            int deltaLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1)); lat += deltaLat;

            shift = 0; result = 0;
            do { b = encodedPath.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
            int deltaLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1)); lng += deltaLng;

            polylinePoints.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return polylinePoints;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) startLocationAndRoute();
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) fusedLocationClient.removeLocationUpdates(locationCallback);
        if (executorService != null) executorService.shutdown();
    }

    private static class RouteData {
        final String distance;
        final String duration;
        final List<LatLng> routePoints;
        RouteData(String distance, String duration, List<LatLng> routePoints) { this.distance = distance; this.duration = duration; this.routePoints = routePoints; }
    }
}
