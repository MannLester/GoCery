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
public class UserFetchTest {
//change commit message
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
                    Log.d("UserFetchTest", "User added successfully");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserFetchTest", "Insert failed: " + e.getMessage(), e);
                    latch.countDown();
                });

        if (!latch.await(10, TimeUnit.SECONDS)) {
            fail("Firestore operation timed out after waiting for 10 seconds");
        }
    }

    @After
    public void teardown() {
        db.collection("users").document(TEST_EMAIL).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("users").document(TEST_EMAIL).delete()
                                .addOnSuccessListener(aVoid -> Log.d("UserFetchTest", "Test user deleted successfully"))
                                .addOnFailureListener(e -> Log.e("UserFetchTest", "Failed to delete test user: " + e.getMessage(), e));
                    }
                })
                .addOnFailureListener(e -> Log.e("UserFetchTest", "Failed to check user existence: " + e.getMessage(), e));
    }
}
