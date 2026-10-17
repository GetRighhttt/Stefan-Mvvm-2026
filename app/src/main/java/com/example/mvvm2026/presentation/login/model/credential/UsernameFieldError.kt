package com.example.mvvm2026.presentation.login.model.credential

/**
 * Identifies the username guidance the UI should display.
 * Domain validation errors are mapped to these presentation values before entering screen state.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
enum class UsernameFieldError { Required, Length, Characters }
