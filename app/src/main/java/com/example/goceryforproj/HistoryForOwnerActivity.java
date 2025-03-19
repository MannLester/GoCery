package com.example.goceryforproj;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryForOwnerActivity extends AppCompatActivity {

    private ListView historyListView;
    private HistoryAdapterOwner historyAdapterOwner;
    private List<HistoryItem> historyItemList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_history);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind Views
        historyListView = findViewById(R.id.historyListView);

        // Initialize List and Adapter
        historyItemList = new ArrayList<>();
        historyAdapterOwner = new HistoryAdapterOwner(this, historyItemList);
        historyListView.setAdapter(historyAdapterOwner);

        // Load Purchase History
        loadPurchaseHistory();
    }

    private void loadPurchaseHistory() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("reports")
                .whereEqualTo("sellerId", currentUserId) // Filter by current user's ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyItemList.clear(); // Clear old data

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String buyerId = document.getString("buyerId");
                        String date = document.getTimestamp("date").toDate().toString(); // Convert Timestamp to String
                        double totalCost = document.getDouble("totalCost");
                        String reportId = document.getId();

                        // Fetch buyer's username from users collection
                        db.collection("users").document(buyerId).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String username = userDoc.getString("username");

                                        // Add item with username
                                        HistoryItem item = new HistoryItem(username, date, totalCost, reportId);
                                        historyItemList.add(item);

                                        // Notify adapter of data change
                                        historyAdapterOwner.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("HistoryActivity", "Error fetching user info", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading history", Toast.LENGTH_SHORT).show();
                    Log.e("HistoryActivity", "Error fetching reports", e);
                });
    }

}
