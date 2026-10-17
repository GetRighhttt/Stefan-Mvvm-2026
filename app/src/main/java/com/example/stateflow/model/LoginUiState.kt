/**
 * Describes everything either renderer needs to draw the current login screen.
 *
 * The ViewModel keeps one value with the inputs, LoginStatus, and clear-confirmation flag. Copying it
 * produces a new StateFlow value that both UI implementations can render after switching or recreation.
 * The status uses sealed objects so each renderer must account for idle, loading, success, and failure.
 *
 * `dialog` derives a single dialog from that state. Clear confirmation takes priority over a result
 * that arrives during loading. Cancelling confirmation exposes the result already in state; confirming
 * clear resets both. LoginDialog names the dialog without depending on Android resources.
 */
package com.example.stateflow.model

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val status: LoginStatus = LoginStatus.Idle,
    val confirmClear: Boolean = false
) {
    val dialog: LoginDialog?
        get() = when {
            confirmClear -> LoginDialog.ClearConfirmation
            status == LoginStatus.Success -> LoginDialog.Success
            status == LoginStatus.Failure -> LoginDialog.Failure
            else -> null
        }
}

sealed interface LoginStatus {
    data object Idle : LoginStatus
    data object Loading : LoginStatus
    data object Success : LoginStatus
    data object Failure : LoginStatus
}

enum class LoginDialog { ClearConfirmation, Success, Failure }
