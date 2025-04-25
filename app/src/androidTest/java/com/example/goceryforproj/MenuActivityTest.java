package com.example.goceryforproj;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.intent.Intents;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import androidx.test.espresso.Root;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

// Helper matcher for Toasts
class ToastMatcher extends TypeSafeMatcher<Root> {
    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }
    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();
            return windowToken == appToken;
        }
        return false;
    }
}

@RunWith(AndroidJUnit4.class)
public class MenuActivityTest {

    private FirebaseFirestore db;
    private static final String TEST_EMAIL = "testuser@gocery.com";
    private static final String TEST_STORE = "testStore123";

    @Rule
    public ActivityScenarioRule<MenuActivity> activityRule =
            new ActivityScenarioRule<>(MenuActivity.class);

    @Before
    public void setup() {
        Intents.init();
        Context context = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();

        // Ensure the user has a store
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("users").document(TEST_EMAIL).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("users").document(TEST_EMAIL)
                                .update("ownedStores", Collections.singletonList(TEST_STORE))
                                .addOnSuccessListener(aVoid -> latch.countDown())
                                .addOnFailureListener(e -> latch.countDown());
                    } else {
                        latch.countDown();
                    }
                })
                .addOnFailureListener(e -> latch.countDown());

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new RuntimeException("Firestore setup timeout");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddStoreNavigation() {
        onView(withId(R.id.btnAddStore)).perform(click());
        intended(hasComponent(AddStore.class.getName()));
    }

    @Test
    public void testAddProductNavigation_blockedWhenNoStore() {
        // Click Add Product when no store exists
        onView(withId(R.id.btnAddProduct)).perform(click());
        // Check for Toast message
        onView(withText("Register a store first!")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testAddProductNavigation_allowedWhenStoreExists() throws InterruptedException {
        // Simulate a user with a store in Firestore
        // For simplicity, insert a dummy store for the test user
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
            .update("ownedStores", java.util.Collections.singletonList("testStore"));
        // Wait for Firestore update to propagate (Espresso IdlingResource is better, but this is simple)
        Thread.sleep(1500);
        // Now click Add Product
        onView(withId(R.id.btnAddProduct)).perform(click());
        intended(hasComponent(AddProduct.class.getName()));
    }

    @Test
    public void testGenerateQRNavigation() {
        onView(withId(R.id.btnGenerateQR)).perform(click());
        intended(hasComponent(GenerateQrActivity.class.getName()));
    }

    @Test
    public void testInventoryNavigation() {
        onView(withId(R.id.btnCheckInventory)).perform(click());
        intended(hasComponent(InventoryActivity.class.getName()));
    }

    @Test
    public void testReceiptScannerNavigation() {
        onView(withId(R.id.btnValidateReceipt)).perform(click());
        intended(hasComponent(ReceiptScanner.class.getName()));
    }

    @Test
    public void testHistoryNavigation() {
        onView(withId(R.id.btnStoreHistory)).perform(click());
        intended(hasComponent(HistoryForOwnerActivity.class.getName()));
    }
    @After
    public void tearDown() {
        Intents.release();
        db.collection("users").document(TEST_EMAIL).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("users").document(TEST_EMAIL).delete()
                                .addOnSuccessListener(aVoid -> Log.d("Test", "Test user deleted"))
                                .addOnFailureListener(e -> Log.e("Test", "Failed to delete test user", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Test", "Failed to check user existence", e));
    }
}
