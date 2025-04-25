package com.example.goceryforproj;

import android.widget.Toast;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class GenerateQRActivityTester {
    @Rule
    public ActivityTestRule<GenerateQrActivity> activityRule = new ActivityTestRule<>(GenerateQrActivity.class, true, false);

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String testStoreId;
    private String testProductId;
    private String testStoreName = "Test QR Store";
    private String testProductName = "Test QR Product";

    @Before
    public void setUp() throws InterruptedException {
        // Sign in anonymously
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        if (auth.getCurrentUser() == null) {
            final CountDownLatch authLatch = new CountDownLatch(1);
            auth.signInAnonymously()
                .addOnCompleteListener(task -> authLatch.countDown());
            authLatch.await(5, TimeUnit.SECONDS);
        }
        
        // Create test store
        final CountDownLatch storeLatch = new CountDownLatch(1);
        String userId = auth.getCurrentUser().getUid();
        
        Map<String, Object> storeData = new HashMap<>();
        storeData.put("storeName", testStoreName);
        storeData.put("ownerId", userId);
        storeData.put("phoneNumber", "1234567890");
        
        db.collection("stores").add(storeData)
            .addOnSuccessListener(documentReference -> {
                testStoreId = documentReference.getId();
                
                // Add store ID to user's owned stores
                db.collection("users").document(userId)
                    .update("ownedStores", java.util.Arrays.asList(testStoreId))
                    .addOnCompleteListener(task -> {
                        // Create test product
                        Map<String, Object> productData = new HashMap<>();
                        productData.put("productName", testProductName);
                        productData.put("price", "9.99");
                        productData.put("weight", "500");
                        
                        db.collection("products").add(productData)
                            .addOnSuccessListener(productRef -> {
                                testProductId = productRef.getId();
                                
                                // Add product to store
                                Map<String, Object> inventoryItem = new HashMap<>();
                                inventoryItem.put("productId", testProductId);
                                inventoryItem.put("inventoryCount", 10);
                                
                                List<Map<String, Object>> productList = new ArrayList<>();
                                productList.add(inventoryItem);
                                
                                db.collection("stores").document(testStoreId)
                                    .update("products", productList)
                                    .addOnCompleteListener(t -> storeLatch.countDown());
                            });
                    });
            });
        
        storeLatch.await(10, TimeUnit.SECONDS);
        
        // Launch activity after setup
        activityRule.launchActivity(null);
        
        // Wait for UI to load
        Thread.sleep(2000);
    }

    @After
    public void tearDown() throws InterruptedException {
        // Clean up test data
        final CountDownLatch cleanupLatch = new CountDownLatch(1);
        
        if (testProductId != null) {
            db.collection("products").document(testProductId).delete();
        }
        
        if (testStoreId != null) {
            db.collection("stores").document(testStoreId).delete()
                .addOnCompleteListener(task -> cleanupLatch.countDown());
        }
        
        cleanupLatch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testStoreSpinnerPopulates() throws InterruptedException {
        // Verify store spinner shows the test store
        onView(withId(R.id.storeSpinner)).check(matches(isDisplayed()));
        onView(withId(R.id.storeSpinner)).check(matches(withSpinnerText(containsString(testStoreName))));
    }

    @Test
    public void testProductSpinnerPopulatesOnStoreSelection() throws InterruptedException {
        // Select the test store
        onView(withId(R.id.storeSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(testStoreName))).perform(click());
        
        // Wait for products to load
        Thread.sleep(1500);
        
        // Verify product spinner is enabled and populated
        onView(withId(R.id.productSpinner)).check(matches(isEnabled()));
        
        // Click product spinner to see options
        onView(withId(R.id.productSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(testProductName))).perform(click());
    }

    @Test
    public void testShowProductInfoAndQRCode() throws InterruptedException {
        // Select test store
        onView(withId(R.id.storeSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(testStoreName))).perform(click());
        
        // Wait for products to load
        Thread.sleep(1500);
        
        // Select test product
        onView(withId(R.id.productSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(testProductName))).perform(click());
        
        // Click show product info
        onView(withId(R.id.btnShowProductInfo)).perform(click());
        
        // Wait for QR code to load
        Thread.sleep(2000);
        
        // Verify product info and QR code are displayed
        onView(withId(R.id.productInfoText)).check(matches(isDisplayed()));
        onView(withId(R.id.productInfoText)).check(matches(withText(containsString(testProductName))));
        onView(withId(R.id.productInfoText)).check(matches(withText(containsString("9.99"))));
        onView(withId(R.id.qrCodeImageView)).check(matches(isDisplayed()));
    }

    @Test
    public void testShowProductInfoWithNoProductSelected() {
        // Try clicking show info with no product selected
        onView(withId(R.id.btnShowProductInfo)).perform(click());
        
        // Check for toast message
        onView(withText("Please select a valid product."))
            .inRoot(RootMatchers.withDecorView(
                org.hamcrest.Matchers.not(activityRule.getActivity().getWindow().getDecorView())))
            .check(matches(isDisplayed()));
    }
}
