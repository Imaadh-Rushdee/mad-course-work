package com.example.pizza_mania_app.helperClasses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GoogleMapsHelper {

    private static final String TAG = "GoogleMapsHelper";

    // Centralized API key
    private static final String API_KEY = "AIzaSyCldldEy5A5sk7K3-RkyHhoCH86XeToP8s";

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1001;

    public static final int BRANCH_COLOMBO = 1;
    public static final int BRANCH_GALLE = 2;

    // Predefined branch locations
    private static final LatLng COLOMBO_BRANCH = new LatLng(6.9271, 79.8612); // Colombo center
    private static final LatLng GALLE_BRANCH = new LatLng(6.0535, 80.2210);   // Galle center

    // Delivery radius in meters
    private static final float COLOMBO_RADIUS = 15000; // 15 km
    private static final float GALLE_RADIUS = 10000;   // 10 km

    // Initialize Places API (call once)
    public static void initPlaces(Context context) {
        if (!Places.isInitialized()) {
            Places.initialize(context.getApplicationContext(), API_KEY);
            Log.d(TAG, "Places API initialized with centralized key");
        }
    }

    // Launch Google Places Autocomplete
    public static void openPlacePicker(Activity activity) {
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY,
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))
                .build(activity);
        activity.startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    public static int getAutocompleteRequestCode() {
        return AUTOCOMPLETE_REQUEST_CODE;
    }

    public static Place getPlaceFromIntent(Intent data) {
        return Autocomplete.getPlaceFromIntent(data);
    }

    public static void addMarker(GoogleMap googleMap, LatLng latLng, String title, boolean moveCamera) {
        googleMap.addMarker(new MarkerOptions().position(latLng).title(title));
        if (moveCamera) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        }
    }

    public static void moveCamera(GoogleMap googleMap, LatLng latLng, float zoom) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public static void showError(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
    }

    // Geocode address to LatLng
    public static LatLng geocodeAddress(Context context, String address) {
        try {
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                return new LatLng(addr.getLatitude(), addr.getLongitude());
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding failed: " + e.getMessage());
        }
        return null;
    }

    // Get branch id based on address using geocoding + distance
    public static int getBranchIdFromAddress(Context context, String address) {
        if (address == null || address.isEmpty()) {
            showError(context, "Invalid address");
            return -1;
        }

        LatLng userLatLng = geocodeAddress(context, address);
        if (userLatLng == null) {
            showError(context, "Unable to locate address");
            return -1;
        }

        if (isWithinRadius(userLatLng, COLOMBO_BRANCH, COLOMBO_RADIUS)) {
            return BRANCH_COLOMBO;
        } else if (isWithinRadius(userLatLng, GALLE_BRANCH, GALLE_RADIUS)) {
            return BRANCH_GALLE;
        } else {
            showError(context, "Delivery not available for your area");
            return -1;
        }
    }

    // Check if user location is within radius of a branch
    private static boolean isWithinRadius(LatLng user, LatLng branch, float radiusMeters) {
        float[] results = new float[1];
        Location.distanceBetween(
                user.latitude, user.longitude,
                branch.latitude, branch.longitude,
                results
        );
        return results[0] <= radiusMeters;
    }
}
