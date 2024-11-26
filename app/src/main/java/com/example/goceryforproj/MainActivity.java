package com.example.goceryforproj;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    TextView name, mail;
    FirebaseFirestore db;
    private static final String TAG = "MainActivity";

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {
                    Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(accountTask);
                } catch (Exception e) {
                    Log.e(TAG, "Sign in result failed", e);
                    Toast.makeText(MainActivity.this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            navigateToMenu();
            return;
        }

        // Initialize UI elements
        name = findViewById(R.id.nameTV);
        mail = findViewById(R.id.mailTV);

        // Google Sign-In configuration
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))  // Use your actual client_id here
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, options);

        // Set up the Google Sign-In button
        SignInButton signInButton = findViewById(R.id.signIn);
        signInButton.setOnClickListener(view -> {
            // Sign out before starting the sign-in process to allow choosing a different account
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // Start the Google sign-in intent
                Intent intent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(intent);
            });
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Google Sign In successful: " + account.getEmail());
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            Log.e(TAG, "signInResult:failed message=" + e.getMessage());
            Log.e(TAG, "Client ID being used: " + getString(R.string.client_id));

            // Initialize with a default value
            String errorMessage = "Unknown error occurred";

            switch (e.getStatusCode()) {
                case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                    errorMessage = "Sign In Cancelled";
                    break;
                case GoogleSignInStatusCodes.NETWORK_ERROR:
                    errorMessage = "Network Error";
                    break;
                case GoogleSignInStatusCodes.INVALID_ACCOUNT:
                    errorMessage = "Invalid Account";
                    break;
                case GoogleSignInStatusCodes.SIGN_IN_REQUIRED:
                    errorMessage = "Sign In Required";
                    break;
                case GoogleSignInStatusCodes.DEVELOPER_ERROR:
                    errorMessage = "Developer Error - Check SHA1 fingerprint and client ID";
                    Log.e(TAG, "Make sure you have:");
                    Log.e(TAG, "1. Added SHA1 fingerprint to Firebase Console");
                    Log.e(TAG, "2. Downloaded latest google-services.json");
                    Log.e(TAG, "3. Using correct client ID in strings.xml");
                    break;
                default:
                    if (e.getStatusMessage() != null) {
                        errorMessage = "Sign In Failed: " + e.getStatusMessage();
                    }
                    break;
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            updateUI(user);
                            saveUserData(user.getUid(), user.getDisplayName(), user.getEmail());
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            name.setText(user.getDisplayName());
            mail.setText(user.getEmail());
            navigateToMenu();
        }
    }

    private void navigateToMenu() {
        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveUserData(String userId, String username, String email) {
        // Reference to the user document in the 'users' collection
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Check if the document exists
                    if (documentSnapshot.exists()) {
                        // Document exists, don't override the data
                        Log.d("MainActivity", "User already exists in Firestore, not overriding data.");
                        Toast.makeText(MainActivity.this, "User data already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        // Document does not exist, save the new user data
                        List<String> ownedStores = Arrays.asList();  // Start with an empty list of owned stores

                        // Create a map of user data
                        User user = new User(username, email, ownedStores);

                        // Save user data to Firestore
                        db.collection("users")
                                .document(userId)  // Use the user ID as the document ID
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("MainActivity", "User data saved successfully");
                                    Toast.makeText(MainActivity.this, "User data saved", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MainActivity", "Error saving user data", e);
                                    Toast.makeText(MainActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error checking if user exists", e);
                    Toast.makeText(MainActivity.this, "Error checking user existence", Toast.LENGTH_SHORT).show();
                });
    }

}
