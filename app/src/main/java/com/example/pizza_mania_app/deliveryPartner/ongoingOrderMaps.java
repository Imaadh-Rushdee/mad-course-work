package com.example.pizza_mania_app.deliveryPartner;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ongoingOrderMaps extends FragmentActivity implements OnMapReadyCallback, TextToSpeech.OnInitListener {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String DIRECTIONS_API_KEY = "YOUR_API_KEY_HERE";
    private static final float ARRIVAL_THRESHOLD_METERS = 50f;
    private static final float JOURNEY_START_THRESHOLD_METERS = 30f;

    // UI Components
    private GoogleMap mMap;
    private ActivityOngoingOrderMapsBinding binding;
    private TextView statusText, distanceText, etaText, nextInstructionText, distanceToInstructionText;
    private ImageButton readyButton, completedButton, centerLocationButton, voiceToggleButton;
    private LinearLayout navigationPanel;

    // Location Services
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // Text-to-Speech
    private TextToSpeech textToSpeech;
    private boolean isTTSEnabled = true;

    // Data
    private SQLiteDatabase database;
    private int orderId;

    // Map Elements
    private LatLng customerLocation;
    private LatLng currentDriverLocation;
    private LatLng initialDriverLocation;
    private Marker driverMarker;
    private Marker customerMarker;
    private Polyline routePolyline;

    // Navigation Data
    private List<NavigationStep> navigationSteps = new ArrayList<>();
    private int currentStepIndex = 0;
    private boolean isNavigationMode = false;

    // Journey State
    private boolean isJourneyStarted = false;
    private boolean isRouteDrawn = false;
    private boolean hasArrivedAtCustomer = false;
    private float distanceToCustomer = Float.MAX_VALUE;

    // Performance
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOngoingOrderMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getIntExtra("orderId", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Invalid order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeComponents();
        setupUI();
        setupMap();
    }

    private void initializeComponents() {
        database = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, this);

        // Setup location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                handleLocationUpdate(locationResult.getLastLocation());
            }
        };
    }

    private void setupUI() {
        // Initialize UI components
        statusText = findViewById(R.id.statusText);
        distanceText = findViewById(R.id.distanceText);
        etaText = findViewById(R.id.etaText);
        readyButton = findViewById(R.id.readyButton);
        completedButton = findViewById(R.id.completedButton);

        // Navigation components
        navigationPanel = findViewById(R.id.navigationPanel);
        nextInstructionText = findViewById(R.id.nextInstructionText);
        distanceToInstructionText = findViewById(R.id.distanceToInstructionText);
        centerLocationButton = findViewById(R.id.centerLocationButton);
        voiceToggleButton = findViewById(R.id.voiceToggleButton);

        // Set initial states
        statusText.setText("Preparing for delivery...");
        distanceText.setText("Calculating distance...");
        etaText.setText("Calculating ETA...");

        // Hide navigation panel initially
        if (navigationPanel != null) {
            navigationPanel.setVisibility(View.GONE);
        }

        // Setup button listeners
        completedButton.setOnClickListener(v -> completeOrder());
        centerLocationButton.setOnClickListener(v -> centerMapOnLocation());
        voiceToggleButton.setOnClickListener(v -> toggleVoiceInstructions());
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
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (checkLocationPermission()) {
            startDeliveryJourney();
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
                startDeliveryJourney();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startDeliveryJourney() {
        loadCustomerLocation();
        startLocationTracking();
        statusText.setText("Starting delivery journey...");
    }

    private void loadCustomerLocation() {
        executorService.execute(() -> {
            try {
                Cursor cursor = database.rawQuery(
                        "SELECT address_latitude, address_longitude FROM orders WHERE order_id = ?",
                        new String[]{String.valueOf(orderId)}
                );

                if (cursor.moveToFirst()) {
                    double latitude = cursor.getDouble(0);
                    double longitude = cursor.getDouble(1);
                    customerLocation = new LatLng(latitude, longitude);
                    mainHandler.post(() -> addCustomerMarker());
                }
                cursor.close();
            } catch (Exception e) {
                Log.e("OngoingOrder", "Error loading customer location", e);
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

    private void startLocationTracking() {
        if (!checkLocationPermission()) return;

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(1000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void handleLocationUpdate(Location location) {
        if (location == null) return;

        currentDriverLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (initialDriverLocation == null) {
            initialDriverLocation = currentDriverLocation;
        }

        updateDriverMarker();

        if (customerLocation != null) {
            calculateDistanceToCustomer();
            checkJourneyStatus();
        }

        // Navigation logic
        if (isNavigationMode && !navigationSteps.isEmpty()) {
            updateNavigationInstructions();
        }

        // Draw route
        if (!isRouteDrawn && customerLocation != null) {
            drawRouteToCustomer();
            isRouteDrawn = true;
        }

        // Update camera
        if (isNavigationMode) {
            updateNavigationCamera();
        } else {
            adjustOverviewCamera();
        }
    }

    private void updateDriverMarker() {
        if (currentDriverLocation == null || mMap == null) return;

        if (driverMarker == null) {
            driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentDriverLocation)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        } else {
            driverMarker.setPosition(currentDriverLocation);
        }
    }

    private void calculateDistanceToCustomer() {
        float[] results = new float[1];
        Location.distanceBetween(
                currentDriverLocation.latitude, currentDriverLocation.longitude,
                customerLocation.latitude, customerLocation.longitude,
                results
        );
        distanceToCustomer = results[0];

        if (distanceToCustomer < 1000) {
            distanceText.setText(String.format("Distance: %.0f m", distanceToCustomer));
        } else {
            distanceText.setText(String.format("Distance: %.1f km", distanceToCustomer / 1000));
        }
    }

    private void checkJourneyStatus() {
        // Check if journey started
        if (!isJourneyStarted && initialDriverLocation != null) {
            float[] distanceFromStart = new float[1];
            Location.distanceBetween(
                    initialDriverLocation.latitude, initialDriverLocation.longitude,
                    currentDriverLocation.latitude, currentDriverLocation.longitude,
                    distanceFromStart
            );

            if (distanceFromStart[0] > JOURNEY_START_THRESHOLD_METERS) {
                startJourney();
            }
        }

        // Check if arrived
        if (isJourneyStarted && !hasArrivedAtCustomer && distanceToCustomer <= ARRIVAL_THRESHOLD_METERS) {
            arriveAtCustomer();
        }
    }

    private void startJourney() {
        isJourneyStarted = true;
        isNavigationMode = true;
        statusText.setText("Navigation started");

        // Show navigation panel
        if (navigationPanel != null) {
            navigationPanel.setVisibility(View.VISIBLE);
        }

        speakInstruction("Navigation started");
        Toast.makeText(this, "Navigation started!", Toast.LENGTH_SHORT).show();
    }

    private void arriveAtCustomer() {
        hasArrivedAtCustomer = true;
        isNavigationMode = false;
        statusText.setText("Arrived at customer location");

        // Hide navigation panel
        if (navigationPanel != null) {
            navigationPanel.setVisibility(View.GONE);
        }

        completedButton.setEnabled(true);
        completedButton.setColorFilter(Color.GREEN);

        speakInstruction("You have arrived at your destination");
        Toast.makeText(this, "You have arrived!", Toast.LENGTH_LONG).show();
    }

    private void updateNavigationInstructions() {
        if (currentStepIndex >= navigationSteps.size()) return;

        NavigationStep currentStep = navigationSteps.get(currentStepIndex);
        LatLng stepLocation = new LatLng(currentStep.latitude, currentStep.longitude);

        // Calculate distance to step
        float[] results = new float[1];
        Location.distanceBetween(
                currentDriverLocation.latitude, currentDriverLocation.longitude,
                stepLocation.latitude, stepLocation.longitude,
                results
        );
        float distanceToStep = results[0];

        // Update UI
        if (nextInstructionText != null) {
            nextInstructionText.setText(currentStep.instruction);
        }

        if (distanceToInstructionText != null) {
            if (distanceToStep < 1000) {
                distanceToInstructionText.setText(String.format("In %.0f m", distanceToStep));
            } else {
                distanceToInstructionText.setText(String.format("In %.1f km", distanceToStep / 1000));
            }
        }

        // Announce instruction
        if (distanceToStep <= 100f && !currentStep.hasBeenAnnounced) {
            speakInstruction(currentStep.instruction);
            currentStep.hasBeenAnnounced = true;
        }

        // Move to next step
        if (distanceToStep <= 20f && currentStepIndex < navigationSteps.size() - 1) {
            currentStepIndex++;
        }
    }

    private void updateNavigationCamera() {
        if (currentDriverLocation == null || mMap == null) return;

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentDriverLocation)
                .zoom(18.0f)
                .tilt(45.0f)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void adjustOverviewCamera() {
        if (currentDriverLocation == null || customerLocation == null || mMap == null) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(currentDriverLocation);
        boundsBuilder.include(customerLocation);

        LatLngBounds bounds = boundsBuilder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
    }

    private void centerMapOnLocation() {
        if (currentDriverLocation != null && mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentDriverLocation, 16.0f));
        }
    }

    private void toggleVoiceInstructions() {
        isTTSEnabled = !isTTSEnabled;
        if (voiceToggleButton != null) {
            voiceToggleButton.setColorFilter(isTTSEnabled ? Color.GREEN : Color.GRAY);
        }
        Toast.makeText(this, isTTSEnabled ? "Voice ON" : "Voice OFF", Toast.LENGTH_SHORT).show();
    }

    private void speakInstruction(String instruction) {
        if (isTTSEnabled && textToSpeech != null) {
            textToSpeech.speak(instruction, TextToSpeech.QUEUE_FLUSH, null, "nav");
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.getDefault());
        }
    }

    private void drawRouteToCustomer() {
        if (currentDriverLocation == null || customerLocation == null) return;

        executorService.execute(() -> {
            try {
                String directionsUrl = buildDirectionsUrl(currentDriverLocation, customerLocation);
                String response = makeHttpRequest(directionsUrl);
                RouteData routeData = parseDirectionsResponse(response);

                if (routeData != null) {
                    mainHandler.post(() -> {
                        drawRoute(routeData.routePoints);
                        navigationSteps = routeData.steps;
                        currentStepIndex = 0;

                        if (routeData.duration != null) {
                            etaText.setText("ETA: " + routeData.duration);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("OngoingOrder", "Error drawing route", e);
            }
        });
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

    private RouteData parseDirectionsResponse(String jsonResponse) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        String status = jsonObject.getString("status");

        if (!"OK".equals(status)) {
            return null;
        }

        JSONArray routes = jsonObject.getJSONArray("routes");
        if (routes.length() == 0) return null;

        JSONObject route = routes.getJSONObject(0);
        JSONArray legs = route.getJSONArray("legs");
        JSONObject leg = legs.getJSONObject(0);

        String duration = leg.getJSONObject("duration").getString("text");
        String polylinePoints = route.getJSONObject("overview_polyline").getString("points");

        List<LatLng> routePoints = decodePolyline(polylinePoints);
        List<NavigationStep> steps = parseNavigationSteps(legs);

        return new RouteData(duration, routePoints, steps);
    }

    private List<NavigationStep> parseNavigationSteps(JSONArray legs) throws Exception {
        List<NavigationStep> steps = new ArrayList<>();

        JSONObject leg = legs.getJSONObject(0);
        JSONArray stepsArray = leg.getJSONArray("steps");

        for (int i = 0; i < stepsArray.length(); i++) {
            JSONObject stepJson = stepsArray.getJSONObject(i);

            String instruction = stepJson.getString("html_instructions")
                    .replaceAll("<[^>]*>", ""); // Remove HTML tags

            JSONObject startLocation = stepJson.getJSONObject("start_location");
            double lat = startLocation.getDouble("lat");
            double lng = startLocation.getDouble("lng");

            NavigationStep step = new NavigationStep(instruction, lat, lng);
            steps.add(step);
        }

        return steps;
    }

    private void drawRoute(List<LatLng> routePoints) {
        if (routePoints == null || routePoints.isEmpty() || mMap == null) return;

        // Remove existing route
        if (routePolyline != null) {
            routePolyline.remove();
        }

        // Draw route
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

    private void completeOrder() {
        if (!hasArrivedAtCustomer) {
            Toast.makeText(this, "You must be near the customer", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                database.execSQL("UPDATE orders SET order_status = ? WHERE order_id = ?",
                        new Object[]{"completed", orderId});

                mainHandler.post(() -> {
                    statusText.setText("Order completed!");
                    Toast.makeText(this, "Order completed!", Toast.LENGTH_SHORT).show();

                    mainHandler.postDelayed(() -> finish(), 2000);
                });
            } catch (Exception e) {
                Log.e("OngoingOrder", "Error completing order", e);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if (database != null && database.isOpen()) {
            database.close();
        }

        if (executorService != null) {
            executorService.shutdown();
        }
    }

    // Simple helper classes
    private static class RouteData {
        final String duration;
        final List<LatLng> routePoints;
        final List<NavigationStep> steps;

        RouteData(String duration, List<LatLng> routePoints, List<NavigationStep> steps) {
            this.duration = duration;
            this.routePoints = routePoints;
            this.steps = steps;
        }
    }

    private static class NavigationStep {
        final String instruction;
        final double latitude;
        final double longitude;
        boolean hasBeenAnnounced = false;

        NavigationStep(String instruction, double latitude, double longitude) {
            this.instruction = instruction;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}