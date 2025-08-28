package com.example.pizza_mania_app.deliveryPartner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania_app.R;

public class ongoingOrder extends AppCompatActivity {

    private ImageView statusPreparingIcon, statusReadyIcon, statusCompletedIcon;
    private Button completeOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ongoing_order);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        statusPreparingIcon = findViewById(R.id.statusPreparingIcon);
        statusReadyIcon = findViewById(R.id.statusReadyIcon);
        statusCompletedIcon = findViewById(R.id.statusCompletedIcon);
        completeOrder = findViewById(R.id.completeOrder);

        // Initially disable button
        completeOrder.setEnabled(false);

        // Default: Preparing active
        statusPreparingIcon.setImageResource(R.drawable.ic_circle_green);
        statusReadyIcon.setImageResource(R.drawable.ic_circle_grey);
        statusCompletedIcon.setImageResource(R.drawable.ic_circle_grey);

        // Simulate status changes with delay
        new Handler().postDelayed(() -> {
            // Change to Ready
            statusReadyIcon.setImageResource(R.drawable.ic_circle_green);
        }, 5000); // 5 sec after load

        new Handler().postDelayed(() -> {
            // Change to Completed
            statusCompletedIcon.setImageResource(R.drawable.ic_circle_green);
            completeOrder.setEnabled(true); // Enable button when completed
        }, 10000); // 10 sec after load
    }

    public void completeOrder(View view) {
        Intent intent = new Intent(this, orderPayment.class);
        startActivity(intent);
    }
}
