package com.example.pizza_mania_app;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania_app.helperClasses.SelectLocationActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public class signUp extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword, etPhone;
    private TextView tvSelectedLocation;
    private SQLiteDatabase db;
    private String selectedAddress = "";

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int MAP_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

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
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        Button btnGetLocation = findViewById(R.id.btnGetLocation);
        Button btnPickLocation = findViewById(R.id.btnPickLocation);
        TextView tvLogin = findViewById(R.id.tvLogin);

        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS users(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "email TEXT," +
                "phone TEXT," +
                "password TEXT," +
                "role TEXT," +
                "address TEXT," +
                "branch_id INTEGER," +
                "profile_pic BLOB)");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnSignUp.setOnClickListener(v -> registerUser());
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(signUp.this, SelectLocationActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        });

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

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                selectedAddress = getAddressFromLocation(location);
                tvSelectedLocation.setText(selectedAddress);
                Toast.makeText(this, "Location detected: " + selectedAddress, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to get location. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getAddressFromLocation(Location location) {
        String result = "";
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                result = addresses.get(0).getAddressLine(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedAddress = data.getStringExtra("selected_address");
            tvSelectedLocation.setText(selectedAddress);
        }
    }
}
