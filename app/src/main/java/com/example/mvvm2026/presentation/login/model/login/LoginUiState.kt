package com.example.mvvm2026.presentation.login.model.login

import com.example.mvvm2026.presentation.login.model.credential.PasswordFieldError
import com.example.mvvm2026.presentation.login.model.credential.UsernameFieldError

/**
 * Holds the inputs, field errors, submission flag, status, and clear-confirmation flag.
 * Both renderers draw the same snapshot. The derived dialog gives clear confirmation priority over
 * a result that arrives during loading, so those dialogs cannot compete.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val status: LoginStatus = LoginStatus.Idle,
    val confirmClear: Boolean = false,
    val validationAttempted: Boolean = false,
    val usernameError: UsernameFieldError? = null,
    val passwordError: PasswordFieldError? = null
) {
    val dialog: LoginDialog?
        get() = when {
            confirmClear -> LoginDialog.ClearConfirmation
            status == LoginStatus.Success -> LoginDialog.Success
            status == LoginStatus.Failure -> LoginDialog.Failure
            else -> null
        }
}
