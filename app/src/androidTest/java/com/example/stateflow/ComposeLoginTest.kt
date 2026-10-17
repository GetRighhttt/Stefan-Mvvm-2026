/**
 * Compares both renderers inside the same activity instead of testing an isolated Composable.
 *
 * The v2 Android Compose test rule runs Compose effects with queued test dispatching. Espresso operates
 * the XML form and host switch; Compose semantics operate text fields, dialogs, and Snackbars after the
 * switch. Tests check shared inputs, an in-flight attempt, renderer restoration, and message delivery
 * without replay.
 *
 * The field helpers select nodes with text-edit actions. `loginButton()` excludes those nodes because
 * the username field and submit button share the Login label. `awaitSuccess()` waits for the actual
 * result semantics with a deadline, so completion depends on the rendered result rather than a fixed
 * sleep. `runOnIdle()` checks state only after Compose has finished its pending work.
 */
package com.example.stateflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.stateflow.model.LoginAction
import com.example.stateflow.model.LoginStatus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ComposeLoginTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun switchingBothWaysKeepsTheSameInputs() {
        onView(withId(R.id.et_login)).perform(replaceText("username"))
        onView(withId(R.id.et_password)).perform(replaceText("password"), closeSoftKeyboard())
        onView(withId(R.id.use_compose)).perform(click())
        compose.onNodeWithText("username").assertIsDisplayed()
        passwordField().performTextReplacement("changed")
        onView(withId(R.id.use_compose)).perform(click())
        onView(withId(R.id.et_login)).check(matches(withText("username")))
        onView(withId(R.id.et_password)).check(matches(withText("changed")))
    }

    @Test
    fun composeLoginShowsAResultAndAcknowledgementClearsInputs() {
        onView(withId(R.id.use_compose)).perform(click())
        usernameField().performTextReplacement("username")
        passwordField().performTextReplacement("password")
        loginButton().performClick()
        awaitSuccess()
        compose.onNodeWithText(compose.activity.getString(android.R.string.ok)).performClick()
        compose.runOnIdle {
            val state = ViewModelProvider(compose.activity)[MainViewModel::class.java].uiState.value
            assertEquals("", state.username)
            assertEquals("", state.password)
            assertEquals(LoginStatus.Idle, state.status)
        }
    }

    @Test
    fun switchingToComposeDuringLoginKeepsTheAttempt() {
        compose.activityRule.scenario.onActivity { activity ->
            ViewModelProvider(activity)[MainViewModel::class.java].apply {
                onAction(LoginAction.UsernameChanged("username"))
                onAction(LoginAction.PasswordChanged("password"))
                onAction(LoginAction.Submit)
            }
            activity.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.use_compose)
                .isChecked = true
        }
        awaitSuccess()
    }

    @Test
    fun recreationKeepsTheComposeSelectionAndInputs() {
        onView(withId(R.id.use_compose)).perform(click())
        usernameField().performTextReplacement("username")
        passwordField().performTextReplacement("password")
        compose.activityRule.scenario.recreate()
        onView(withId(R.id.use_compose)).check(matches(isChecked()))
        compose.onNodeWithText("username").assertIsDisplayed()
        onView(withId(R.id.use_compose)).perform(click())
        onView(withId(R.id.et_password)).check(matches(withText("password")))
    }

    @Test
    fun validationMessagesDoNotReplayWhenSwitchingButNewMessagesArrive() {
        onView(withId(R.id.btn_login)).perform(click())
        onView(withText(R.string.missing_credentials)).check(matches(isDisplayed()))
        onView(withId(R.id.use_compose)).perform(click())
        compose.onNodeWithText(compose.activity.getString(R.string.missing_credentials)).assertDoesNotExist()
        loginButton().performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.missing_credentials)).assertIsDisplayed()
    }

    @Test
    fun confirmingClearInComposeClearsTheXmlFormToo() {
        onView(withId(R.id.use_compose)).perform(click())
        usernameField().performTextReplacement("username")
        passwordField().performTextReplacement("password")
        compose.onNode(hasClickAction() and hasText(compose.activity.getString(R.string.clear))).performClick()
        compose.onNodeWithText(compose.activity.getString(android.R.string.ok)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.form_cleared)).assertIsDisplayed()
        onView(withId(R.id.use_compose)).perform(click())
        onView(withId(R.id.et_login)).check(matches(withText("")))
        onView(withId(R.id.et_password)).check(matches(withText("")))
    }

    private fun usernameField() = compose.onNode(hasSetTextAction() and hasText(compose.activity.getString(R.string.login)))
    private fun passwordField() = compose.onNode(hasSetTextAction() and hasText(compose.activity.getString(R.string.password)))
    private fun loginButton() = compose.onNode(
        hasClickAction() and hasText(compose.activity.getString(R.string.login)) and !hasSetTextAction()
    )

    private fun awaitSuccess() {
        val message = compose.activity.getString(R.string.login_success)
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(message).assertIsDisplayed()
    }
}
