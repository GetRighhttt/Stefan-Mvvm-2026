/**
 * Owns the form and login attempt independently of the selected UI toolkit.
 *
 * `onAction()` is the entry point for both screens. Input actions update `uiState`; confirmation and
 * result actions change the dialog state or reset the form. Keeping those decisions here gives the
 * XML and Compose implementations the same behavior.
 *
 * `login()` checks blank inputs, ignores another submission while loading or showing a dialog, and
 * captures the credentials before the delayed check. It updates the existing state so a clear
 * confirmation opened during loading still takes priority when the result arrives. `clear()` cancels
 * the job before resetting the form, so an earlier attempt cannot produce a later result.
 *
 * Explicit backing fields keep the public flow types and mutable storage in one declaration.
 * Callers see `uiState` as StateFlow and `effects` as SharedFlow. Inside this class, Kotlin uses the
 * inferred MutableStateFlow and MutableSharedFlow field types for `update()`, value assignment, and
 * `tryEmit()`. This is a compile-time API boundary; each getter returns its mutable field object
 * directly rather than wrapping it in another read-only flow object.
 *
 * The state holds inputs, status, and dialogs. Effects carry temporary validation and clear messages.
 * Zero replay and a bounded buffer allow messages to be dropped when no screen is collecting or a
 * collector falls behind. Results that need acknowledgement stay in state.
 */
package com.example.stateflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stateflow.model.Login
import com.example.stateflow.model.LoginAction
import com.example.stateflow.model.LoginEffect
import com.example.stateflow.model.LoginStatus
import com.example.stateflow.model.LoginUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val uiState: StateFlow<LoginUiState>
        field = MutableStateFlow(LoginUiState())

    val effects: SharedFlow<LoginEffect>
        field = MutableSharedFlow<LoginEffect>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    private var loginJob: Job? = null

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.UsernameChanged -> uiState.update { it.copy(username = action.value) }
            is LoginAction.PasswordChanged -> uiState.update { it.copy(password = action.value) }
            LoginAction.Submit -> login()
            LoginAction.RequestClear -> uiState.update { it.copy(confirmClear = true) }
            LoginAction.CancelClear -> uiState.update { it.copy(confirmClear = false) }
            LoginAction.ConfirmClear -> {
                clear()
                effects.tryEmit(LoginEffect.FormCleared)
            }
            LoginAction.DismissResult -> clear()
        }
    }

    private fun login() {
        val state = uiState.value
        if (state.status == LoginStatus.Loading || state.dialog != null) return
        if (state.username.isBlank() || state.password.isBlank()) {
            effects.tryEmit(LoginEffect.MissingCredentials)
            return
        }
        val credentials = Login(state.username, state.password)
        uiState.update { it.copy(status = LoginStatus.Loading) }
        loginJob = viewModelScope.launch {
            delay(1_000L)
            val result = if (credentials.username == "username" && credentials.password == "password") {
                LoginStatus.Success
            } else {
                LoginStatus.Failure
            }
            uiState.update { it.copy(status = result) }
        }
    }

    private fun clear() {
        loginJob?.cancel()
        loginJob = null
        uiState.value = LoginUiState()
    }
}
