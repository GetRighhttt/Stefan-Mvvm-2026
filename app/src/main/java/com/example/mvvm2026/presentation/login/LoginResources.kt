package com.example.mvvm2026.presentation.login

import android.content.res.Resources
import androidx.annotation.StringRes
import com.example.mvvm2026.R
import com.example.mvvm2026.domain.usecase.ValidateCredentials
import com.example.mvvm2026.presentation.login.model.login.LoginDialog
import com.example.mvvm2026.presentation.login.model.login.LoginEffect
import com.example.mvvm2026.presentation.login.model.credential.PasswordFieldError
import com.example.mvvm2026.presentation.login.model.credential.UsernameFieldError

/**
 * Maps dialogs, temporary messages, and validation errors to text shared by XML and Compose.
 * Requirement text uses the domain length limits. Null field errors restore the normal guidance;
 * Android resource handling stays here so domain models remain independent of presentation.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
@get:StringRes
val LoginDialog.messageResource: Int
    get() = when (this) {
        LoginDialog.ClearConfirmation -> R.string.clear_confirmation
        LoginDialog.Success -> R.string.login_success
        LoginDialog.Failure -> R.string.login_failure
    }

fun Resources.effectMessage(effect: LoginEffect): String = when (effect) {
    LoginEffect.ValidationFailed -> getString(R.string.validation_failed)
    LoginEffect.FormCleared -> getString(R.string.form_cleared)
}

val Resources.usernameRequirements: String
    get() = getString(R.string.username_requirements,
        ValidateCredentials.USERNAME_MIN_LENGTH, ValidateCredentials.USERNAME_MAX_LENGTH)

val Resources.passwordRequirements: String
    get() = getString(R.string.password_requirements,
        ValidateCredentials.PASSWORD_MIN_LENGTH, ValidateCredentials.PASSWORD_MAX_LENGTH)

fun Resources.usernameErrorText(error: UsernameFieldError?): String? = when (error) {
    null -> null
    UsernameFieldError.Required -> getString(R.string.username_required)
    UsernameFieldError.Length -> getString(R.string.username_length,
        ValidateCredentials.USERNAME_MIN_LENGTH, ValidateCredentials.USERNAME_MAX_LENGTH)
    UsernameFieldError.Characters -> getString(R.string.username_characters)
}

fun Resources.passwordErrorText(error: PasswordFieldError?): String? = when (error) {
    null -> null
    PasswordFieldError.Required -> getString(R.string.password_required)
    PasswordFieldError.Length -> getString(R.string.password_length,
        ValidateCredentials.PASSWORD_MIN_LENGTH, ValidateCredentials.PASSWORD_MAX_LENGTH)
    PasswordFieldError.Whitespace -> getString(R.string.password_whitespace)
    PasswordFieldError.Complexity -> getString(R.string.password_complexity)
}
