package com.example.mvvm2026.domain.usecase

import com.example.mvvm2026.domain.model.credential.CredentialValidation
import com.example.mvvm2026.domain.model.login.Login
import com.example.mvvm2026.domain.model.credential.PasswordError
import com.example.mvvm2026.domain.model.credential.UsernameError

/**
 * Applies the sample username and password policies and returns typed errors.
 * The combined call checks both fields before submission. Field methods support revalidation during
 * editing, and shared constants keep the requirement text consistent with the enforced length limits.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
class ValidateCredentials {
    operator fun invoke(credentials: Login) = CredentialValidation(
        usernameError = usernameError(credentials.username),
        passwordError = passwordError(credentials.password)
    )

    fun usernameError(value: String): UsernameError? = when {
        value.isBlank() -> UsernameError.Required
        value.length !in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH -> UsernameError.Length
        !value.all { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it == '_' } -> UsernameError.Characters
        else -> null
    }

    fun passwordError(value: String): PasswordError? = when {
        value.isBlank() -> PasswordError.Required
        value.length !in PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH -> PasswordError.Length
        value.any(Char::isWhitespace) -> PasswordError.Whitespace
        !value.any(Char::isUpperCase) || !value.any(Char::isLowerCase) ||
            !value.any(Char::isDigit) || !value.any { !it.isLetterOrDigit() } -> PasswordError.Complexity
        else -> null
    }

    companion object {
        const val USERNAME_MIN_LENGTH = 3
        const val USERNAME_MAX_LENGTH = 20
        const val PASSWORD_MIN_LENGTH = 8
        const val PASSWORD_MAX_LENGTH = 64
    }
}
