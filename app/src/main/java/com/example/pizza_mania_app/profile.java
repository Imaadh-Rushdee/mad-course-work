package com.example.pizza_mania_app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class profile extends AppCompatActivity {

    private SQLiteDatabase db;
    private ImageView imgProfile;
    private TextView tvUsername;
    private LinearLayout llEditDetails, llManageAddresses, llSavedCards, llDefaultBranch;

    private String loggedInEmail = "admin@pizza.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the profile layout
        setContentView(R.layout.activity_profile);

        // Initialize views
        imgProfile = findViewById(R.id.imgProfile);
        tvUsername = findViewById(R.id.tvUsername);
        llEditDetails = findViewById(R.id.llEditDetails);
        llManageAddresses = findViewById(R.id.llManageAddresses);
        llSavedCards = findViewById(R.id.llSavedCards);
        llDefaultBranch = findViewById(R.id.llDefaultBranch);

        // Open or create database
        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        // Load logged-in user data
        loadUserData();
    }

    /** Fetch user data from database and display in UI */
    private void loadUserData() {
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email=?", new String[]{loggedInEmail});

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String profilePic = cursor.getString(cursor.getColumnIndexOrThrow("profile_pic"));

            tvUsername.setText(name);

            // Set profile picture if available
            if (profilePic != null && !profilePic.isEmpty()) {
                int resId = getResources().getIdentifier(profilePic, "drawable", getPackageName());
                if (resId != 0) imgProfile.setImageResource(resId);
            }
        } else {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }
}
