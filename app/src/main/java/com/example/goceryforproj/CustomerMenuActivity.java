package com.example.goceryforproj;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

public class CustomerMenuActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView welcomeText;
    private Button btnProfile, btnCart, btnHistory;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_menu);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind Views
        profileImage = findViewById(R.id.profile_image);
        welcomeText = findViewById(R.id.welcome_text);
        btnCart = findViewById(R.id.btn_cart);
        btnHistory = findViewById(R.id.btn_history);

        // Load user info
        loadUserInfo();

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerMenuActivity.this, HistoryForCustomerActivity.class);
            startActivity(intent);
        });

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerMenuActivity.this, ConsumerActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    String profilePicUrl = documentSnapshot.getString("profilePic");

                    // Set the username
                    if (username != null) {
                        welcomeText.setText("Happy Shopping, " + username + "!");
                    }

                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Glide.with(this)
                                .load(profilePicUrl)
                                .transform(new CircleCrop()) // Force circular shape
                                .placeholder(R.drawable.ic_profile_placeholder) // While loading
                                .error(R.drawable.ic_profile_placeholder)      // On error
                                .into(profileImage);
                    } else {
                        // Default circular profile image if URL is empty
                        Glide.with(this)
                                .load(R.drawable.ic_profile_placeholder)
                                .transform(new CircleCrop())
                                .into(profileImage);
                    }
                } else {
                    welcomeText.setText("User data not found");
                }
            }).addOnFailureListener(e -> {
                welcomeText.setText("Error loading profile");
            });
        }
    }
}
