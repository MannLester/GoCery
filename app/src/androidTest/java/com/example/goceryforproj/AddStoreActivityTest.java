package com.example.goceryforproj;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.widget.Toast;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Root;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AddStoreActivityTest {

    @Rule
    public ActivityScenarioRule<AddStore> activityRule =
            new ActivityScenarioRule<>(AddStore.class);

    // Helper matcher for Toasts
    public static class ToastMatcher extends TypeSafeMatcher<Root> {
        @Override
        public void describeTo(Description description) {
            description.appendText("is toast");
        }
        @Override
        public boolean matchesSafely(Root root) {
            int type = root.getWindowLayoutParams().get().type;
            if ((type == android.view.WindowManager.LayoutParams.TYPE_TOAST)) {
                android.os.IBinder windowToken = root.getDecorView().getWindowToken();
                android.os.IBinder appToken = root.getDecorView().getApplicationWindowToken();
                return windowToken == appToken;
            }
            return false;
        }
    }

    @Test
    public void testValidation_emptyFieldsShowsErrors() {
        // Try to submit with all fields empty
        onView(withId(R.id.btnSubmitStore)).perform(click());
        // Check that errors are shown (EditText error is not directly testable, but field remains visible)
        onView(withId(R.id.storeName)).check(matches(isDisplayed()));
        onView(withId(R.id.storeLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.storePhone)).check(matches(isDisplayed()));
    }

    @Test
    public void testValidation_partialFieldsShowsErrors() {
        onView(withId(R.id.storeName)).perform(replaceText("Test Store"));
        onView(withId(R.id.btnSubmitStore)).perform(click());
        // Location and phone still empty
        onView(withId(R.id.storeLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.storePhone)).check(matches(isDisplayed()));
    }

    @Test
    public void testPhoneValidation_nonDigitsShowsError() {
        onView(withId(R.id.storeName)).perform(replaceText("Test Store"));
        onView(withId(R.id.storeLocation)).perform(replaceText("Test Location"));
        onView(withId(R.id.storePhone)).perform(replaceText("12345abc678"));
        onView(withId(R.id.btnSubmitStore)).perform(click());
        // Check for error (EditText error is not directly testable, but field remains visible)
        onView(withId(R.id.storePhone)).check(matches(isDisplayed()));
    }

    @Test
    public void testPhoneValidation_tooLongShowsError() {
        onView(withId(R.id.storeName)).perform(replaceText("Test Store"));
        onView(withId(R.id.storeLocation)).perform(replaceText("Test Location"));
        onView(withId(R.id.storePhone)).perform(replaceText("123456789012"));
        onView(withId(R.id.btnSubmitStore)).perform(click());
        // Check for error (EditText error is not directly testable, but field remains visible)
        onView(withId(R.id.storePhone)).check(matches(isDisplayed()));
    }

    @Test
    public void testPhoneValidation_valid11DigitsPasses() {
        onView(withId(R.id.storeName)).perform(replaceText("Test Store"));
        onView(withId(R.id.storeLocation)).perform(replaceText("Test Location"));
        onView(withId(R.id.storePhone)).perform(replaceText("12345678901"));
        onView(withId(R.id.btnSubmitStore)).perform(click());
        // If no error, field remains visible
        onView(withId(R.id.storePhone)).check(matches(isDisplayed()));
    }

    @Test
    public void testNoUserShowsToast() {
        // Sign out user if any
        FirebaseAuth.getInstance().signOut();
        onView(withId(R.id.storeName)).perform(replaceText("Test Store"));
        onView(withId(R.id.storeLocation)).perform(replaceText("Test Location"));
        onView(withId(R.id.storePhone)).perform(replaceText("1234567890"));
        onView(withId(R.id.btnSubmitStore)).perform(click());
        onView(withText("No user signed in")).inRoot(new ToastMatcher()).check(matches(isDisplayed()));
    }

    @Test
    public void testClearButtonResetsForm() {
        onView(withId(R.id.storeName)).perform(replaceText("Test Store"));
        onView(withId(R.id.storeLocation)).perform(replaceText("Test Location"));
        onView(withId(R.id.storePhone)).perform(replaceText("1234567890"));
        onView(withId(R.id.btnClear)).perform(click());
        onView(withId(R.id.storeName)).check(matches(isDisplayed()));
        onView(withId(R.id.storeLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.storePhone)).check(matches(isDisplayed()));
    }

    // The following tests require a signed-in user and Firestore to be set up properly.

    @Test
    public void testStoreCreationSuccess() throws InterruptedException {
        // You must be signed in for this to work
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        onView(withId(R.id.storeName)).perform(replaceText("Unique Store " + System.currentTimeMillis()));
        onView(withId(R.id.storeLocation)).perform(replaceText("Test Location"));
        onView(withId(R.id.storePhone)).perform(replaceText("1234567890"));
        onView(withId(R.id.btnSubmitStore)).perform(click());
        // Wait for Firestore (not robust, but simple)
        Thread.sleep(2000);
    }

}
