package com.example.mvvm2026.presentation.login

import com.example.mvvm2026.domain.model.account.Account
import com.example.mvvm2026.domain.model.credential.PasswordError
import com.example.mvvm2026.domain.model.credential.UsernameError
import com.example.mvvm2026.presentation.login.model.account.AccountUiModel
import com.example.mvvm2026.presentation.login.model.credential.PasswordFieldError
import com.example.mvvm2026.presentation.login.model.credential.UsernameFieldError

/**
 * Maps domain accounts and validation errors into presentation models.
 * Explicit mappings let the UI and domain representations evolve independently.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
fun Account.toUiModel(): AccountUiModel = AccountUiModel(id = id, displayName = name)

fun UsernameError.toUiError(): UsernameFieldError = when (this) {
    UsernameError.Required -> UsernameFieldError.Required
    UsernameError.Length -> UsernameFieldError.Length
    UsernameError.Characters -> UsernameFieldError.Characters
}

fun PasswordError.toUiError(): PasswordFieldError = when (this) {
    PasswordError.Required -> PasswordFieldError.Required
    PasswordError.Length -> PasswordFieldError.Length
    PasswordError.Whitespace -> PasswordFieldError.Whitespace
    PasswordError.Complexity -> PasswordFieldError.Complexity
}
