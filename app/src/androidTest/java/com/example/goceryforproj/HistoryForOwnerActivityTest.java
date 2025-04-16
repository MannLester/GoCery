package com.example.goceryforproj;

import android.content.Intent;
import android.widget.ListView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class HistoryForOwnerActivityTest {
    
    @Rule
    public ActivityTestRule<HistoryForOwnerActivity> activityRule = 
            new ActivityTestRule<>(HistoryForOwnerActivity.class, true, false);
    
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String testReportId;
    private String testBuyerId;
    private double testTotalCost = 25.99;
    
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
        
        // Create test buyer user
        final CountDownLatch buyerLatch = new CountDownLatch(1);
        testBuyerId = "test-buyer-" + System.currentTimeMillis();
        
        Map<String, Object> buyerData = new HashMap<>();
        buyerData.put("username", "TestBuyer");
        buyerData.put("email", "testbuyer@example.com");
        
        db.collection("users").document(testBuyerId)
            .set(buyerData)
            .addOnCompleteListener(task -> buyerLatch.countDown());
        
        buyerLatch.await(5, TimeUnit.SECONDS);
        
        // Create test report
        final CountDownLatch reportLatch = new CountDownLatch(1);
        String sellerId = auth.getCurrentUser().getUid();
        
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("buyerId", testBuyerId);
        reportData.put("sellerId", sellerId);
        reportData.put("date", new com.google.firebase.Timestamp(new Date()));
        reportData.put("totalCost", testTotalCost);
        
        db.collection("reports").add(reportData)
            .addOnSuccessListener(documentReference -> {
                testReportId = documentReference.getId();
                reportLatch.countDown();
            });
        
        reportLatch.await(5, TimeUnit.SECONDS);
    }
    
    @After
    public void tearDown() throws InterruptedException {
        // Clean up test data
        final CountDownLatch cleanupLatch = new CountDownLatch(1);
        
        if (testReportId != null) {
            db.collection("reports").document(testReportId).delete();
        }
        
        if (testBuyerId != null) {
            db.collection("users").document(testBuyerId).delete()
                .addOnCompleteListener(task -> cleanupLatch.countDown());
        }
        
        cleanupLatch.await(5, TimeUnit.SECONDS);
    }
    
    @Test
    public void testActivityLoadsSuccessfully() {
        // Launch activity
        activityRule.launchActivity(null);
        
        // Verify ListView is displayed
        onView(withId(R.id.historyListView)).check(matches(isDisplayed()));
    }
    
    @Test
    public void testPurchaseHistoryLoadsForOwner() throws InterruptedException {
        // Launch activity
        activityRule.launchActivity(null);
        
        // Wait for Firestore data to load
        Thread.sleep(3000);
        
        // Verify data in ListView
        ActivityScenario<HistoryForOwnerActivity> scenario = 
                ActivityScenario.launch(HistoryForOwnerActivity.class);
        
        scenario.onActivity(activity -> {
            ListView listView = activity.findViewById(R.id.historyListView);
            assertTrue("ListView should have at least one item", listView.getAdapter().getCount() > 0);
            
            // Check if our test report is in the list
            boolean found = false;
            for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                HistoryItem item = (HistoryItem) listView.getAdapter().getItem(i);
                if (item.getReportId().equals(testReportId)) {
                    found = true;
                    assertEquals("TestBuyer", item.getBuyerName());
                    assertEquals(testTotalCost, item.getTotalCost(), 0.01);
                    break;
                }
            }
            assertTrue("Test report should be in the list", found);
        });
    }
    
    @Test
    public void testEmptyHistoryHandling() throws InterruptedException {
        // Clean up any existing reports for this user
        final CountDownLatch cleanLatch = new CountDownLatch(1);
        String sellerId = auth.getCurrentUser().getUid();
        
        db.collection("reports")
            .whereEqualTo("sellerId", sellerId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> reportIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    reportIds.add(doc.getId());
                }
                
                if (reportIds.isEmpty()) {
                    cleanLatch.countDown();
                } else {
                    int[] count = {reportIds.size()};
                    for (String id : reportIds) {
                        db.collection("reports").document(id).delete()
                            .addOnCompleteListener(task -> {
                                count[0]--;
                                if (count[0] == 0) {
                                    cleanLatch.countDown();
                                }
                            });
                    }
                }
            });
        
        cleanLatch.await(10, TimeUnit.SECONDS);
        
        // Launch activity with empty history
        activityRule.launchActivity(null);
        
        // Wait for Firestore query to complete
        Thread.sleep(2000);
        
        // Verify empty state
        ActivityScenario<HistoryForOwnerActivity> scenario = 
                ActivityScenario.launch(HistoryForOwnerActivity.class);
        
        scenario.onActivity(activity -> {
            ListView listView = activity.findViewById(R.id.historyListView);
            assertEquals("ListView should be empty", 0, listView.getAdapter().getCount());
        });
    }
    
    // Note: Testing Firestore failure would require mocking Firestore,
    // which is beyond the scope of this basic test implementation
}
