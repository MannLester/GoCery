package com.example.goceryforproj;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.textfield.TextInputEditText;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

@RunWith(AndroidJUnit4.class)
public class DarkModeTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testProductNameTextColorIsVisibleInDarkMode() {
        // Type sample text
        onView(withId(R.id.productName)).perform(typeText("Dark mode test"));

        // Check the text color is not white (avoid white-on-white issue)
        onView(withId(R.id.productName)).check(matches(new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                if (view instanceof EditText) {
                    int textColor = ((EditText) view).getCurrentTextColor();
                    return textColor != Color.WHITE; // FAIL if text is white on white bg
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Expected text color to NOT be white");
            }
        }));
    }
}
