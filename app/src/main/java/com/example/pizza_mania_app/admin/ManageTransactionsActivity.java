package com.example.pizza_mania_app.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pizza_mania_app.R;

public class ManageTransactionsActivity extends AppCompatActivity {

    EditText etTransactionId, etTransactionAmount;
    Button btnAddTransaction, btnUpdateTransaction, btnDeleteTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_transactions);

        etTransactionId = findViewById(R.id.etTransactionId);
        etTransactionAmount = findViewById(R.id.etTransactionAmount);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);
        btnUpdateTransaction = findViewById(R.id.btnUpdateTransaction);
        btnDeleteTransaction = findViewById(R.id.btnDeleteTransaction);

        btnAddTransaction.setOnClickListener(v ->
                Toast.makeText(this, "Transaction Added: " + etTransactionId.getText(), Toast.LENGTH_SHORT).show());

        btnUpdateTransaction.setOnClickListener(v ->
                Toast.makeText(this, "Transaction Updated", Toast.LENGTH_SHORT).show());

        btnDeleteTransaction.setOnClickListener(v ->
                Toast.makeText(this, "Transaction Deleted", Toast.LENGTH_SHORT).show());
    }
}
