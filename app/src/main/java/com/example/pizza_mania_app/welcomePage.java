package com.example.pizza_mania_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class welcomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainWelcome), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnLoginWelcome = findViewById(R.id.btnLoginWelcome);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvGuest = findViewById(R.id.tvGuest);

        // Login Button → Navigate to Login Page
        btnLoginWelcome.setOnClickListener(v -> {
            Intent intent = new Intent(welcomePage.this, loginPage.class);
            startActivity(intent);
        });

        // Sign Up Button → (Create signup activity later)
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(welcomePage.this, signUp.class);
            startActivity(intent);
        });

        // Guest Option → (You can send to main menu / home later)
        tvGuest.setOnClickListener(v -> {
            Intent intent = new Intent(welcomePage.this, menu.class);
            startActivity(intent);
        });
    }
}
