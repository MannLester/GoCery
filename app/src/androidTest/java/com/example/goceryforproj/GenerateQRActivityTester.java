package com.example.goceryforproj;

import android.widget.Toast;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class GenerateQRActivityTester {
    @Rule
    public ActivityTestRule<GenerateQrActivity> activityRule = new ActivityTestRule<>(GenerateQrActivity.class);

    @Test
    public void testStoreSpinnerPopulates() {
        onView(withId(R.id.storeSpinner)).check(matches(isDisplayed()));
        // Optionally: check spinner has expected items
    }

    @Test
    public void testNoStoresAvailable() {
        // Simulate user with no stores (requires setup/mocking)
        onView(withId(R.id.storeSpinner)).check(matches(isDisplayed()));
        // Check product spinner disabled
        onView(withId(R.id.productSpinner)).check(matches(isDisplayed()));
        // Optionally: check for Toast or message
    }

    @Test
    public void testProductSpinnerPopulatesOnStoreSelection() {
        // Select a store (requires test data)
        onView(withId(R.id.storeSpinner)).perform(click());
        // onData(anything()).atPosition(0).perform(click()); // Uncomment if using AdapterView
        onView(withId(R.id.productSpinner)).check(matches(isDisplayed()));
    }

    @Test
    public void testNoProductsForStore() {
        // Simulate store with no products (requires setup/mocking)
        onView(withId(R.id.storeSpinner)).perform(click());
        // onData(anything()).atPosition(0).perform(click());
        onView(withId(R.id.productSpinner)).check(matches(isDisplayed()));
        // Optionally: check for Toast or message
    }

    @Test
    public void testShowProductInfoAndQRCode() {
        // Select store and product (requires test data)
        onView(withId(R.id.storeSpinner)).perform(click());
        // onData(anything()).atPosition(0).perform(click());
        onView(withId(R.id.productSpinner)).perform(click());
        // onData(anything()).atPosition(0).perform(click());
        onView(withId(R.id.btnShowProductInfo)).perform(click());
        onView(withId(R.id.productInfoText)).check(matches(isDisplayed()));
        onView(withId(R.id.qrCodeImageView)).check(matches(isDisplayed()));
    }

    @Test
    public void testShowProductInfoWithNoProductSelected() {
        // Try clicking show info with no product selected
        onView(withId(R.id.btnShowProductInfo)).perform(click());
        onView(withText("Please select a valid product.")).inRoot(RootMatchers.withDecorView(
                org.hamcrest.Matchers.not(activityRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void testFirestoreFailureOnFetchingStores() {
        // Requires mocking Firestore failure
        // Placeholder: check for Toast or UI element
        onView(withId(R.id.storeSpinner)).check(matches(isDisplayed()));
    }

    @Test
    public void testFirestoreFailureOnFetchingProducts() {
        // Requires mocking Firestore failure
        // Placeholder: check for Toast or UI element
        onView(withId(R.id.productSpinner)).check(matches(isDisplayed()));
    }

    @Test
    public void testFirestoreFailureOnFetchingProductInfo() {
        // Requires mocking Firestore failure
        // Placeholder: check for Toast or UI element
        onView(withId(R.id.productInfoText)).check(matches(isDisplayed()));
    }

    @Test
    public void testQRCodeGenerationFailure() {
        // Simulate error in QR code generation (may require code modification)
        // Placeholder: check for Toast or UI element
        onView(withId(R.id.qrCodeImageView)).check(matches(isDisplayed()));
    }
}
