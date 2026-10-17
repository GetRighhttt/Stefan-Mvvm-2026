package com.example.mvvm2026.presentation.login.model.credential

/**
 * Identifies the password guidance the UI should display.
 * Presentation owns these values and their resource mapping independently of domain validation errors.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
enum class PasswordFieldError { Required, Length, Whitespace, Complexity }
