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

import android.os.IBinder;
import android.view.WindowManager;
import android.view.View;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import androidx.test.espresso.Root;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

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

    @Rule
    public ActivityScenarioRule<MenuActivity> activityRule =
            new ActivityScenarioRule<>(MenuActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
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
        onView(withId(R.id.btnHistory)).perform(click());
        intended(hasComponent(HistoryForOwnerActivity.class.getName()));
    }
}
