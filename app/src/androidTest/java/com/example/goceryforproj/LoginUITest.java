package com.example.goceryforproj;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
//update commit message
@RunWith(AndroidJUnit4.class)
public class LoginUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testGoogleSignInButton() {
        // Check if Google Sign-In button is displayed
        onView(withId(R.id.signIn)).check(matches(isDisplayed()));

        // Simulate a click on the Google Sign-In button
        onView(withId(R.id.signIn)).perform(click());

        // After signing in, check if user is redirected to the correct dashboard
        // Check for the Customer Dashboard
        onView(withId(R.id.welcome_text)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserTypeSelectionAsCustomer() {
        // Select "Customer" from the user type dropdown
        onView(withId(R.id.actvUserType)).perform(click());
        onView(withText("Customer")).perform(click());

        // Simulate the sign-in process after selecting "Customer"
        onView(withId(R.id.signIn)).perform(click());

        // Check if the user is redirected to the Customer Dashboard
        // Example: Check for a unique element from the customer dashboard (e.g., "User Cart" button)
        onView(withId(R.id.btn_cart)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserTypeSelectionAsAdmin() {
        // Select "Admin" from the user type dropdown
        onView(withId(R.id.actvUserType)).perform(click());
        onView(withText("Admin")).perform(click());

        // Simulate the sign-in process after selecting "Admin"
        onView(withId(R.id.signIn)).perform(click());

        // Check if the user is redirected to the Admin Dashboard
        // Example: Check for a unique element from the admin dashboard (e.g., "Add Store" button)
        onView(withId(R.id.btnAddStore)).check(matches(isDisplayed()));
    }
}
