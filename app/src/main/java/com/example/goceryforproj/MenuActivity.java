package com.example.goceryforproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private Button btnAddStore, btnAddProduct, btnGenerateQR, btnInventory, btnreceipt, btnHistory;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getSupportActionBar().hide();  // Hides the action bar

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize buttons
        btnAddStore = findViewById(R.id.btnAddStore);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnGenerateQR = findViewById(R.id.btnGenerateQR);
        btnInventory = findViewById(R.id.btnCheckInventory);
        btnreceipt = findViewById(R.id.btnValidateReceipt);
        btnHistory = findViewById(R.id.btnStoreHistory);

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, HistoryForOwnerActivity.class);
                startActivity(intent);
            }
        });
        // Set onClickListeners for navigation
        btnAddStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AddStore activity
                Intent intent = new Intent(MenuActivity.this, AddStore.class);
                startActivity(intent);
            }
        });

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoresAndNavigate();
            }
        });

        btnGenerateQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to GenerateQR activity
                Intent intent = new Intent(MenuActivity.this, GenerateQrActivity.class);
                startActivity(intent);
            }
        });
        btnInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to GenerateQR activity
                Intent intent = new Intent(MenuActivity.this, InventoryActivity.class);
                startActivity(intent);
            }
        });
        btnreceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to GenerateQR activity
                Intent intent = new Intent(MenuActivity.this, ReceiptScanner.class);
                startActivity(intent);
            }
        });
    }

    private void checkStoresAndNavigate() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> ownedStores = (List<String>) documentSnapshot.get("ownedStores");
                        if (ownedStores != null && !ownedStores.isEmpty()) {
                            // User has stores, navigate to AddProduct
                            Intent intent = new Intent(MenuActivity.this, AddProduct.class);
                            startActivity(intent);
                        } else {
                            // No stores found
                            Toast.makeText(MenuActivity.this, "Register a store first!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MenuActivity.this, "Error checking stores", Toast.LENGTH_SHORT).show();
                });
    }
}