package com.example.mvvm2026.presentation.login.model.login

/**
 * Names the dialog selected from screen state without storing resources or UI objects.
 * ClearConfirmation takes priority; success or failure can be shown after that confirmation is cancelled.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
enum class LoginDialog {
    ClearConfirmation,
    Success,
    Failure
}
