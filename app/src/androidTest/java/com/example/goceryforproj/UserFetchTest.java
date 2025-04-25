package com.example.goceryforproj;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EmailFetchTest {

    private static final String TAG = "DatabaseTest";
    private FirebaseFirestore db;
    private static final String TEST_EMAIL = "test@example.com";

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(context);  // Ensure Firebase is initialized
        db = FirebaseFirestore.getInstance();
    }

    @Test
    public void insertAndFetchUser() throws InterruptedException {
        User user = new User("test_user", TEST_EMAIL, null, "profile_pic_url");

        CountDownLatch latch = new CountDownLatch(1);

        db.collection("users")
                .document(user.getEmail())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User added successfully");

                    db.collection("users")
                            .document(user.getEmail())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    User fetchedUser = documentSnapshot.toObject(User.class);
                                    assertNotNull("Fetched user should not be null", fetchedUser);
                                    assertEquals("Username mismatch", "test_user", fetchedUser.getUsername());
                                    assertEquals("Email mismatch", TEST_EMAIL, fetchedUser.getEmail());
                                    Log.d(TAG, "User fetched and verified successfully");
                                } else {
                                    Log.e(TAG, "User not found in Firestore");
                                    fail("User not found in Firestore");
                                }
                                latch.countDown();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Fetch failed: " + e.getMessage(), e);
                                fail("Failed to fetch user: " + e.getMessage());
                                latch.countDown();
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Insert failed: " + e.getMessage(), e);
                    fail("Failed to add user: " + e.getMessage());
                    latch.countDown();
                });

        if (!latch.await(10, TimeUnit.SECONDS)) {
            fail("Firestore operation timed out");
        }
    }

    @After
    public void teardown() {
        db.collection("users").document(TEST_EMAIL).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Test user deleted successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete test user: " + e.getMessage(), e));
    }
}
