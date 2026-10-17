/**
 * Checks the XML renderer through the real activity, fragment lifecycle, and dialog windows.
 *
 * Each test closes its ActivityScenario after interacting with the form through Espresso. The cases
 * cover clear and cancel actions, restored inputs and dialogs, result acknowledgement, and a result
 * arriving behind clear confirmation.
 *
 * `awaitLogin()` observes the shared state and uses a bounded latch on the test thread. The main thread
 * keeps running the login coroutine and rendering the result; the test does not guess a sleep duration.
 * `enterCredentials()` and `assertCredentials()` keep the repeated form steps in one place.
 */
package com.example.stateflow

import com.example.stateflow.model.LoginAction
import com.example.stateflow.model.LoginStatus
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

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
        onView(withId(R.id.et_password)).perform(replaceText("password"), closeSoftKeyboard())
    }

    private fun assertCredentials() {
        onView(withId(R.id.et_login)).check(matches(withText("username")))
        onView(withId(R.id.et_password)).check(matches(withText("password")))
    }
}
