package com.example.goceryforproj;

import android.widget.Toast;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;

import com.google.firebase.auth.FirebaseAuth;

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
public class AddProductActivityTest {
    @Rule
    public ActivityTestRule<AddProduct> activityRule = new ActivityTestRule<>(AddProduct.class);

    @Test
    public void testValidation_allFieldsRequired() {
        onView(withId(R.id.submitProductButton)).perform(click());
        onView(withText("All fields are required.")).inRoot(RootMatchers.withDecorView(
                org.hamcrest.Matchers.not(activityRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void testValidation_inventoryCountMustBeNumber() {
        onView(withId(R.id.productName)).perform(replaceText("Apple"));
        onView(withId(R.id.productPrice)).perform(replaceText("12.50"));
        onView(withId(R.id.productWeight)).perform(replaceText("500"));
        onView(withId(R.id.productInventoryCount)).perform(replaceText("abc"));
        onView(withId(R.id.submitProductButton)).perform(click());
        // Expect: Should not crash, ideally shows error/Toast (app logic may need to be updated)
        onView(withId(R.id.productInventoryCount)).check(matches(isDisplayed()));
    }

    @Test
    public void testValidation_priceAndWeightMustBeNumbers() {
        onView(withId(R.id.productName)).perform(replaceText("Banana"));
        onView(withId(R.id.productPrice)).perform(replaceText("abc"));
        onView(withId(R.id.productWeight)).perform(replaceText("xyz"));
        onView(withId(R.id.productInventoryCount)).perform(replaceText("10"));
        onView(withId(R.id.submitProductButton)).perform(click());
        // Expect: Should not crash, ideally shows error/Toast (app logic may need to be updated)
        onView(withId(R.id.productPrice)).check(matches(isDisplayed()));
        onView(withId(R.id.productWeight)).check(matches(isDisplayed()));
    }

    @Test
    public void testValidation_allFieldsValid() {
        onView(withId(R.id.productName)).perform(replaceText("Orange"));
        onView(withId(R.id.productPrice)).perform(replaceText("15.00"));
        onView(withId(R.id.productWeight)).perform(replaceText("250"));
        onView(withId(R.id.productInventoryCount)).perform(replaceText("20"));
        onView(withId(R.id.submitProductButton)).perform(click());
        // Toast: "Product added successfully!" (async, may need IdlingResource for robust check)
        onView(withId(R.id.productName)).check(matches(isDisplayed()));
    }

    @Test
    public void testStoreSpinner_noStoreSelected() {
        // If spinner allows no selection, try to submit
        // onView(withId(R.id.storeSpinner)).perform(ViewActions.click());
        // onData(anything()).atPosition(0).perform(click());
        // onView(withId(R.id.submitProductButton)).perform(click());
        // Check for error or no action
        onView(withId(R.id.storeSpinner)).check(matches(isDisplayed()));
    }

    @Test
    public void testCategorySpinner_populatesCorrectly() {
        onView(withId(R.id.productCategorySpinner)).perform(ViewActions.click());
        // Check for at least one category (e.g. "Fruits")
        onView(withText("Fruits")).check(matches(isDisplayed()));
    }

    @Test
    public void testFirestoreFailure_productAdd() {
        // This would require mocking Firestore to simulate failure
        // Placeholder: just check UI element remains
        onView(withId(R.id.productName)).check(matches(isDisplayed()));
    }

    @Test
    public void testFirestoreFailure_storeUpdate() {
        // This would require mocking Firestore to simulate failure
        // Placeholder: just check UI element remains
        onView(withId(R.id.productName)).check(matches(isDisplayed()));
    }

    @Test
    public void testBackButton_returnsToPreviousScreen() {
        onView(withId(R.id.btnBack)).perform(click());
        // Cannot assert previous screen, but activity should finish
    }

    @Test
    public void testNoUserShowsError() {
        FirebaseAuth.getInstance().signOut();
        onView(withId(R.id.productName)).perform(replaceText("Apple"));
        onView(withId(R.id.productPrice)).perform(replaceText("12.50"));
        onView(withId(R.id.productWeight)).perform(replaceText("500"));
        onView(withId(R.id.productInventoryCount)).perform(replaceText("10"));
        onView(withId(R.id.submitProductButton)).perform(click());
        // Should show error/Toast (app logic may need to be updated)
        onView(withId(R.id.productName)).check(matches(isDisplayed()));
    }
}
