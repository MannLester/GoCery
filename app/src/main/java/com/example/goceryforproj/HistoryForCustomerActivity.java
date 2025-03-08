package com.example.goceryforproj;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryForCustomerActivity extends AppCompatActivity {

    private ListView historyListView;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyItemList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_history);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind Views
        historyListView = findViewById(R.id.historyListView);

        // Initialize List and Adapter
        historyItemList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(this, historyItemList);
        historyListView.setAdapter(historyAdapter);

        // Load Purchase History
        loadPurchaseHistory();
    }

    private void loadPurchaseHistory() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("reports")
                .whereEqualTo("buyerId", currentUserId) // Filter by current user's ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyItemList.clear(); // Clear old data

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String storeName = document.getString("storeName");
                        String date = document.getTimestamp("date").toDate().toString(); // Convert Timestamp to String
                        double totalCost = document.getDouble("totalCost");

                        HistoryItem item = new HistoryItem(storeName, date, totalCost, document.getId());
                        historyItemList.add(item);
                    }

                    // Notify adapter of data change
                    historyAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading history", Toast.LENGTH_SHORT).show();
                    Log.e("HistoryActivity", "Error fetching reports", e);
                });
    }
}
