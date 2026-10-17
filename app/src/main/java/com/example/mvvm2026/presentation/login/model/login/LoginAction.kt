package com.example.mvvm2026.presentation.login.model.login

/**
 * Groups the form inputs and intent cases accepted by the ViewModel.
 * Edit actions carry raw field values. Clear request, confirmation, and cancellation remain distinct
 * so opening a dialog does not reset inputs or cancel an attempt before the user decides.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
sealed interface LoginAction {
    data class UsernameChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data object Submit : LoginAction
    data object RequestClear : LoginAction
    data object ConfirmClear : LoginAction
    data object CancelClear : LoginAction
    data object DismissResult : LoginAction
}
