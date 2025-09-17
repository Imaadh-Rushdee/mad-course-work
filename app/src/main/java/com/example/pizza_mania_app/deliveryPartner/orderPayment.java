package com.example.pizza_mania_app.deliveryPartner;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania_app.R;

public class orderPayment extends AppCompatActivity {

    private int orderId;
    private SQLiteDatabase db;
    private TextView txtOrderId, txtCustomerName, txtOrderItem, txtAddress,
            txtPaymentMethod, txtOrderDate, txtOrderTime, txtTotalAmount;
    private Button btnPaymentComplete, btnDownloadReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_payment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderId = getIntent().getIntExtra("orderId", -1);
        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);

        txtOrderId = findViewById(R.id.orderId);
        txtCustomerName = findViewById(R.id.customerName);
        txtOrderItem = findViewById(R.id.orderItem);
        txtAddress = findViewById(R.id.address);
        txtPaymentMethod = findViewById(R.id.paymentMethod);
        txtOrderDate = findViewById(R.id.orderDate);
        txtOrderTime = findViewById(R.id.orderTime);
        txtTotalAmount = findViewById(R.id.totalAmount);
        btnPaymentComplete = findViewById(R.id.btnPaymentComplete);
        btnDownloadReceipt = findViewById(R.id.btnDownloadReceipt);

        setDetails();
    }

    private void setDetails() {
        Cursor cursor = db.rawQuery("SELECT * FROM orders WHERE order_id=?",
                new String[]{String.valueOf(orderId)});
        if(cursor.moveToFirst()) {
            txtOrderId.setText(String.valueOf(orderId));
            txtCustomerName.setText(cursor.getString(cursor.getColumnIndexOrThrow("customer_name")));
            txtOrderItem.setText(cursor.getString(cursor.getColumnIndexOrThrow("order_cart")));
            txtAddress.setText(cursor.getString(cursor.getColumnIndexOrThrow("order_address")));
            txtPaymentMethod.setText(cursor.getString(cursor.getColumnIndexOrThrow("payment_method")));
            txtOrderDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("order_date")));
            txtOrderTime.setText(cursor.getString(cursor.getColumnIndexOrThrow("order_time")));
            txtTotalAmount.setText("Rs. " + cursor.getDouble(cursor.getColumnIndexOrThrow("total")));
        }
        cursor.close();
    }

    public void completePayment(View view) {
        ContentValues values = new ContentValues();
        values.put("order_status", "paid");
        int rows = db.update("orders", values, "order_id = ?", new String[]{String.valueOf(orderId)});

        if(rows > 0){
            Toast.makeText(this, "Payment Completed", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, deliveryPartnerDashboard.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error updating payment", Toast.LENGTH_SHORT).show();
        }
    }

    public void downloadReceipt(View view){
        Cursor cursor = db.rawQuery("SELECT * FROM orders WHERE order_id=?",
                new String[]{String.valueOf(orderId)});
        if(cursor.moveToFirst()){
            String customer = cursor.getString(cursor.getColumnIndexOrThrow("customer_name"));
            String items = cursor.getString(cursor.getColumnIndexOrThrow("order_cart"));
            double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            Toast.makeText(this, "Receipt PDF generation not implemented yet", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }
}
