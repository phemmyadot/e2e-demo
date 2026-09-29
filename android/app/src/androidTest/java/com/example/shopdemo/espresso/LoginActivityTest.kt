package com.example.shopdemo.espresso

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.shopdemo.R
import com.example.shopdemo.TestData
import com.example.shopdemo.launchLogin
import com.example.shopdemo.resetApp
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

/** Espresso against the View-based (XML) login screen. */
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    private fun enterCredentials(email: String, password: String) {
        onView(withId(R.id.login_email)).perform(typeText(email))
        onView(withId(R.id.login_password)).perform(typeText(password))
        closeSoftKeyboard()
    }

    @Test
    fun coldLaunchShowsLogin() {
        resetApp()
        launchLogin().use {
            onView(withId(R.id.login_title)).check(matches(withText("Welcome to ShopDemo")))
            onView(withId(R.id.login_error)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        }
    }

    @Test
    fun signInDisabledUntilBothFieldsFilled() {
        resetApp()
        launchLogin().use {
            onView(withId(R.id.login_submit)).check(matches(not(isEnabled())))
            onView(withId(R.id.login_email)).perform(typeText(TestData.EMAIL))
            closeSoftKeyboard()
            onView(withId(R.id.login_submit)).check(matches(not(isEnabled())))
            onView(withId(R.id.login_password)).perform(typeText(TestData.PASSWORD))
            closeSoftKeyboard()
            onView(withId(R.id.login_submit)).check(matches(isEnabled()))
        }
    }

    @Test
    fun wrongPasswordShowsError() {
        resetApp()
        launchLogin().use {
            enterCredentials(TestData.EMAIL, "nope")
            onView(withId(R.id.login_submit)).perform(click())

            onView(withId(R.id.login_error)).check(matches(withText("Invalid email or password")))
            onView(withId(R.id.login_title)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun lockedAccountShowsError() {
        resetApp()
        launchLogin().use {
            enterCredentials(TestData.LOCKED_EMAIL, TestData.PASSWORD)
            onView(withId(R.id.login_submit)).perform(click())

            onView(withId(R.id.login_error)).check(matches(withText("This account is locked")))
        }
    }

    @Test
    fun showsProgressAndDisablesSubmitWhileSigningIn() {
        resetApp(latencyMs = 3_000)
        launchLogin().use {
            enterCredentials(TestData.EMAIL, TestData.PASSWORD)
            onView(withId(R.id.login_submit)).perform(click())

            onView(withId(R.id.login_progress)).check(matches(isDisplayed()))
            onView(withId(R.id.login_submit)).check(matches(not(isEnabled())))
        }
    }
}
