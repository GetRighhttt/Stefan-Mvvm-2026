/**
 * Defines the input contract shared by the XML listeners, Compose callbacks, and both dialog UIs.
 *
 * UsernameChanged and PasswordChanged carry the latest field values. Submit requests a login;
 * RequestClear, ConfirmClear, and CancelClear distinguish opening a confirmation from acting on it.
 * DismissResult acknowledges or cancels a result and resets the form.
 *
 * These actions call `MainViewModel.onAction()` directly. They are UI input, while LoginEffect is the
 * separate output stream for temporary messages sent back to the active screen.
 */
package com.example.stateflow.model

sealed interface LoginAction {
    data class UsernameChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data object Submit : LoginAction
    data object RequestClear : LoginAction
    data object ConfirmClear : LoginAction
    data object CancelClear : LoginAction
    data object DismissResult : LoginAction
}
