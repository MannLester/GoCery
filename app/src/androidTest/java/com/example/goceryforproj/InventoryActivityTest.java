package com.example.goceryforproj;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class InventoryActivityTest {
    @Rule
    public ActivityTestRule<InventoryActivity> activityRule = new ActivityTestRule<>(InventoryActivity.class, true, false);

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String testStoreId;
    private String testProductId;
    private String testStoreName = "Test Inventory Store";
    private String testProductName = "Test Inventory Product";

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
                        productData.put("price", "15.99");
                        productData.put("weight", "750");
                        
                        db.collection("products").add(productData)
                            .addOnSuccessListener(productRef -> {
                                testProductId = productRef.getId();
                                
                                // Add product to store
                                Map<String, Object> inventoryItem = new HashMap<>();
                                inventoryItem.put("productId", testProductId);
                                inventoryItem.put("inventoryCount", 25);
                                
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
    public void testInventoryLoadsOnStoreSelection() throws InterruptedException {
        // Select the test store
        onView(withId(R.id.storeSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(testStoreName))).perform(click());
        
        // Wait for inventory to load
        Thread.sleep(2000);
        
        // Verify inventory layout contains product info
        onView(withId(R.id.inventoryLayout)).check(matches(isDisplayed()));
        
        // Check that product name, price and stock are displayed
        onView(withText(containsString(testProductName))).check(matches(isDisplayed()));
        onView(withText(containsString("15.99"))).check(matches(isDisplayed()));
        onView(withText(containsString("25"))).check(matches(isDisplayed()));
    }

    @Test
    public void testCreateEmptyStoreWithNoInventory() throws InterruptedException {
        // Create a new empty store
        final CountDownLatch emptyStoreLatch = new CountDownLatch(1);
        String userId = auth.getCurrentUser().getUid();
        String emptyStoreName = "Empty Test Store";
        
        Map<String, Object> emptyStoreData = new HashMap<>();
        emptyStoreData.put("storeName", emptyStoreName);
        emptyStoreData.put("ownerId", userId);
        emptyStoreData.put("phoneNumber", "9876543210");
        
        String[] emptyStoreId = new String[1];
        
        db.collection("stores").add(emptyStoreData)
            .addOnSuccessListener(documentReference -> {
                emptyStoreId[0] = documentReference.getId();
                emptyStoreLatch.countDown();
            });
        
        emptyStoreLatch.await(5, TimeUnit.SECONDS);
        
        // Restart activity to refresh store list
        activityRule.finishActivity();
        Thread.sleep(1000);
        activityRule.launchActivity(null);
        Thread.sleep(2000);
        
        // Select the empty store
        onView(withId(R.id.storeSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(emptyStoreName))).perform(click());
        
        // Wait for inventory check
        Thread.sleep(1500);
        
        // Verify "No inventory found" toast appears
        onView(withText("No inventory found for this store."))
            .inRoot(RootMatchers.withDecorView(
                org.hamcrest.Matchers.not(activityRule.getActivity().getWindow().getDecorView())))
            .check(matches(isDisplayed()));
        
        // Clean up the empty store
        if (emptyStoreId[0] != null) {
            db.collection("stores").document(emptyStoreId[0]).delete();
        }
    }

    @Test
    public void testBackButtonFinishesActivity() {
        // Click back button
        onView(withId(R.id.btnBack)).perform(click());
        
        // Activity should finish (this is hard to test directly in Espresso)
        // But we can check that the activity is finishing
        assert(activityRule.getActivity().isFinishing());
    }
}
