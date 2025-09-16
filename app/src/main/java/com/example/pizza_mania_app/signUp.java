package com.example.pizza_mania_app;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.gms.common.api.Status;

import java.util.Arrays;

public class signUp extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword, etPhone;
    private SQLiteDatabase db;
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Handle Edge-to-Edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.logoImg), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone = findViewById(R.id.etPhone);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvLogin = findViewById(R.id.tvLogin);

        // Initialize the new Places API
        //Animation Places = null;
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled (getApplicationContext(), "AIzaSyCldldEy5A5sk7K3-RkyHhoCH86XeToP8s");
        }

        // Autocomplete fragment
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS
        ));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                selectedAddress = place.getAddress();
                Log.d("SignUpPlace", "Selected Address: " + selectedAddress);
            }

            @Override
            public void onError(Status status) {
                Log.e("SignUpPlaceError", "Places API error: " + status.getStatusMessage());
                Toast.makeText(signUp.this, "Error fetching address", Toast.LENGTH_SHORT).show();
            }
        });

        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        btnSignUp.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(signUp.this, loginPage.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = selectedAddress.trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email=?", new String[]{email});
        if (cursor.moveToFirst()) {
            Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("phone", phone.isEmpty() ? null : phone);
        values.put("password", password);
        values.put("role", "customer");
        values.put("address", address);
        values.put("branch_id", 1);
        values.putNull("profile_pic");

        long result = db.insert("users", null, values);

        if (result != -1) {
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(signUp.this, loginPage.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed, try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
