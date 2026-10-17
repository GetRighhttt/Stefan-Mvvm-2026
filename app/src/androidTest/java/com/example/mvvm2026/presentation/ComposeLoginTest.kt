package com.example.mvvm2026.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import com.example.mvvm2026.R
import com.example.mvvm2026.presentation.login.model.account.AccountUiState
import com.example.mvvm2026.presentation.login.MainViewModel
import com.example.mvvm2026.presentation.login.model.login.LoginAction
import com.example.mvvm2026.presentation.login.model.login.LoginStatus
import com.example.mvvm2026.presentation.login.passwordRequirements
import com.example.mvvm2026.presentation.login.usernameRequirements
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

/**
 * Compares both renderers under the same activity and ViewModel lifetime.
 * Espresso operates XML and the host switch; Compose semantics check the other renderer. Tests cover
 * form recovery, temporary messages, and account-notice dismissal across renderer changes and recreation.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
class ComposeLoginTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun switchingBothWaysKeepsTheSameInputs() {
        onView(withId(R.id.et_login)).perform(replaceText("username"))
        onView(withId(R.id.et_password)).perform(replaceText("Password1!"), closeSoftKeyboard())
        onView(withId(R.id.use_compose)).perform(click())
        compose.onNode(hasSetTextAction() and hasText("username")).assertIsDisplayed()
        passwordField().performTextReplacement("changed")
        onView(withId(R.id.use_compose)).perform(click())
        onView(withId(R.id.et_login)).check(matches(withText("username")))
        onView(withId(R.id.et_password)).check(matches(withText("changed")))
    }

    @Test
    fun composeLoginShowsAResultAndAcknowledgementClearsInputs() {
        onView(withId(R.id.use_compose)).perform(click())
        usernameField().performTextReplacement("username")
        passwordField().performTextReplacement("Password1!")
        loginButton().performScrollTo().assertIsDisplayed().performClick()
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
                onAction(LoginAction.PasswordChanged("Password1!"))
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
        passwordField().performTextReplacement("Password1!")
        compose.activityRule.scenario.recreate()
        onView(withId(R.id.use_compose)).check(matches(isChecked()))
        compose.onNode(hasSetTextAction() and hasText("username")).assertIsDisplayed()
        onView(withId(R.id.use_compose)).perform(click())
        onView(withId(R.id.et_password)).check(matches(withText("Password1!")))
    }

    @Test
    fun validationMessagesDoNotReplayWhenSwitchingButNewMessagesArrive() {
        onView(withId(R.id.btn_login)).perform(click())
        onView(withText(R.string.validation_failed)).check(matches(isDisplayed()))
        onView(withId(R.id.use_compose)).perform(click())
        compose.onNodeWithText(compose.activity.getString(R.string.validation_failed)).assertDoesNotExist()
        loginButton().performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.validation_failed)).assertIsDisplayed()
    }

    @Test
    fun confirmingClearInComposeClearsTheXmlFormToo() {
        onView(withId(R.id.use_compose)).perform(click())
        usernameField().performTextReplacement("username")
        passwordField().performTextReplacement("Password1!")
        compose.onNode(hasClickAction() and hasText(compose.activity.getString(R.string.clear)))
            .performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText(compose.activity.getString(android.R.string.ok)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.form_cleared)).assertIsDisplayed()
        onView(withId(R.id.use_compose)).perform(click())
        onView(withId(R.id.et_login)).check(matches(withText("")))
        onView(withId(R.id.et_password)).check(matches(withText("")))
    }

    @Test
    fun fieldErrorsFollowTheRendererAndClearWhenInputBecomesValid() {
        onView(withId(R.id.btn_login)).perform(click())
        onView(withText(R.string.username_required)).check(matches(isDisplayed()))
        onView(withId(R.id.use_compose)).perform(click())
        compose.onNodeWithText(compose.activity.getString(R.string.username_required)).assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.password_required)).assertIsDisplayed()
        usernameField().performTextReplacement("User_Name")
        compose.onNodeWithText(compose.activity.getString(R.string.username_required)).assertDoesNotExist()
        passwordField().performTextReplacement("password1!")
        compose.onNodeWithText(compose.activity.getString(R.string.password_complexity)).assertIsDisplayed()
        passwordField().performTextReplacement("Password1!")
        compose.onNodeWithText(compose.activity.getString(R.string.password_complexity)).assertDoesNotExist()
        onView(withId(R.id.use_compose)).perform(click())
        onView(withId(R.id.et_login)).check(matches(withText("User_Name")))
        compose.runOnIdle {
            val state = ViewModelProvider(compose.activity)[MainViewModel::class.java].uiState.value
            assertEquals(null, state.usernameError)
            assertEquals(null, state.passwordError)
            assertEquals(LoginStatus.Idle, state.status)
        }
    }

    @Test
    fun composeShowsUsernamePasswordRequirementsAndTheMixedCaseSample() {
        onView(withId(R.id.use_compose)).perform(click())
        compose.onNodeWithText(compose.activity.getString(R.string.sample_credentials_title)).assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.sample_username)).assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.sample_password)).assertIsDisplayed()
        compose.onNodeWithText(compose.activity.resources.usernameRequirements).assertIsDisplayed()
        compose.onNodeWithText(compose.activity.resources.passwordRequirements).assertIsDisplayed()
    }

    @Test
    fun accountNoticeSurvivesRendererChangesAndRecreationUntilDismissedInCompose() {
        val viewModel = awaitAccounts()
        val message = compose.activity.resources.getQuantityString(R.plurals.accounts_loaded, 6, 6)
        val title = compose.activity.getString(R.string.account_info_success_title)
        onView(withId(R.id.btn_dismiss_account)).perform(scrollTo())
        onView(withId(R.id.account_heading)).check(matches(withText(title)))
        onView(withText(message)).check(matches(isDisplayed()))
        onView(withId(R.id.use_compose)).perform(click())
        compose.onNodeWithText(title).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(message).assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText(title).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.account_info_dismiss))
            .performScrollTo().performClick()
        compose.onNodeWithText(title).assertDoesNotExist()
        compose.runOnIdle {
            assertFalse((viewModel.accountInfo.value as AccountUiState.Loaded).isNoticeVisible)
        }
        onView(withId(R.id.use_compose)).perform(click())
        onView(withId(R.id.account_card)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun dismissingTheXmlAccountNoticeKeepsItHiddenAcrossRecreationAndRenderers() {
        awaitAccounts()
        onView(withId(R.id.btn_dismiss_account)).perform(scrollTo(), click())
        onView(withId(R.id.account_card)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        compose.activityRule.scenario.recreate()
        onView(withId(R.id.account_card)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.use_compose)).perform(click())
        compose.onNodeWithText(compose.activity.getString(R.string.account_info_success_title))
            .assertDoesNotExist()
    }

    @Test
    fun theAccountNoticeHidesWhenItsCountdownFinishes() {
        onView(withId(R.id.use_compose)).perform(click())
        val viewModel = awaitAccounts()
        val title = compose.activity.getString(R.string.account_info_success_title)
        compose.onNodeWithText(title).performScrollTo().assertIsDisplayed()
        val secondsRemaining = compose.runOnIdle {
            (viewModel.accountInfo.value as AccountUiState.Loaded).dismissalSecondsRemaining
        }
        compose.waitUntil(timeoutMillis = (secondsRemaining + 5) * 1_000L) {
            (viewModel.accountInfo.value as? AccountUiState.Loaded)?.isNoticeVisible == false
        }
        compose.onNodeWithText(title).assertDoesNotExist()
        onView(withId(R.id.use_compose)).perform(click())
        onView(withId(R.id.account_card)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    private fun usernameField() = compose.onNode(hasSetTextAction() and hasText(compose.activity.getString(R.string.username)))
    private fun passwordField() = compose.onNode(hasSetTextAction() and hasText(compose.activity.getString(R.string.password)))
    private fun loginButton() = compose.onNode(
        hasClickAction() and hasText(compose.activity.getString(R.string.login)) and !hasSetTextAction()
    )

    private fun awaitAccounts(): MainViewModel {
        val viewModel = compose.runOnIdle { ViewModelProvider(compose.activity)[MainViewModel::class.java] }
        compose.waitUntil(timeoutMillis = 10_000) { viewModel.accountInfo.value is AccountUiState.Loaded }
        return viewModel
    }

    private fun awaitSuccess() {
        val message = compose.activity.getString(R.string.login_success)
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(message).assertIsDisplayed()
    }
}
