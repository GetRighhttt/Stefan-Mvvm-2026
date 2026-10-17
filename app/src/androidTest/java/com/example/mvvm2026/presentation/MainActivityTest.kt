package com.example.mvvm2026.presentation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mvvm2026.R
import com.example.mvvm2026.domain.model.login.Login
import com.example.mvvm2026.presentation.login.MainViewModel
import com.example.mvvm2026.presentation.login.model.login.LoginAction
import com.example.mvvm2026.presentation.login.model.login.LoginStatus
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the XML form, dialog actions, and recreation through the real activity.
 * A state observer waits for the actual login result while leaving the Android main thread free.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Test
    fun clearConfirmationClearsInputs() {
        ActivityScenario.launch(MainActivity::class.java).use {
            enterCredentials()
            onView(withId(R.id.btn_clear)).perform(click())
            onView(withText(R.string.clear_confirmation)).check(matches(isDisplayed()))
            onView(withText(android.R.string.ok)).perform(click())
            onView(withId(R.id.et_login)).check(matches(withText("")))
            onView(withId(R.id.et_password)).check(matches(withText("")))
        }
    }

    @Test
    fun cancellingClearKeepsInputs() {
        ActivityScenario.launch(MainActivity::class.java).use {
            enterCredentials()
            onView(withId(R.id.btn_clear)).perform(click())
            onView(withText(android.R.string.cancel)).perform(click())
            assertCredentials()
        }
    }

    @Test
    fun recreatingActivityKeepsInputsAndConfirmation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            enterCredentials()
            onView(withId(R.id.btn_clear)).perform(click())
            scenario.recreate()
            onView(withText(R.string.clear_confirmation)).check(matches(isDisplayed()))
            onView(withText(android.R.string.cancel)).perform(click())
            assertCredentials()
        }
    }

    @Test
    fun resultSurvivesRecreationAndAcknowledgementClearsTheForm() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            enterCredentials()
            onView(withId(R.id.btn_login)).perform(click())
            awaitLogin(scenario)
            onView(withText(R.string.login_success)).check(matches(isDisplayed()))
            scenario.recreate()
            onView(withText(R.string.login_success)).check(matches(isDisplayed()))
            onView(withText(android.R.string.ok)).perform(click())
            onView(withId(R.id.et_login)).check(matches(withText("")))
            onView(withId(R.id.et_password)).check(matches(withText("")))
        }
    }

    @Test
    fun resultWaitsForClearConfirmationToBeCancelled() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            enterCredentials()
            scenario.onActivity {
                ViewModelProvider(it)[MainViewModel::class.java].onAction(LoginAction.Submit)
                it.findViewById<android.view.View>(R.id.btn_clear).performClick()
            }
            awaitLogin(scenario)
            onView(withText(R.string.clear_confirmation)).check(matches(isDisplayed()))
            onView(withText(android.R.string.cancel)).perform(click())
            onView(withText(R.string.login_success)).check(matches(isDisplayed()))
        }
    }

    private fun awaitLogin(scenario: ActivityScenario<MainActivity>) {
        val completed = CountDownLatch(1)
        var observer: Job? = null
        scenario.onActivity { activity ->
            observer = activity.lifecycleScope.launch {
                ViewModelProvider(activity)[MainViewModel::class.java].uiState.first {
                    it.status != LoginStatus.Loading
                }
                completed.countDown()
            }
        }
        try {
            assertTrue("Login did not complete", completed.await(5, TimeUnit.SECONDS))
        } finally {
            observer?.cancel()
        }
    }

    private fun enterCredentials() {
        onView(withId(R.id.et_login)).perform(replaceText("username"))
        onView(withId(R.id.et_password)).perform(replaceText("Password1!"), closeSoftKeyboard())
    }

    private fun assertCredentials() {
        onView(withId(R.id.et_login)).check(matches(withText("username")))
        onView(withId(R.id.et_password)).check(matches(withText("Password1!")))
    }
}
