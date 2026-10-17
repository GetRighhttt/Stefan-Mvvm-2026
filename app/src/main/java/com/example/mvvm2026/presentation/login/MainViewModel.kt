package com.example.mvvm2026.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvm2026.domain.model.account.AccountResult
import com.example.mvvm2026.domain.model.login.Login
import com.example.mvvm2026.domain.model.login.LoginResult
import com.example.mvvm2026.domain.repository.LoginRepository
import com.example.mvvm2026.domain.usecase.ValidateCredentials
import com.example.mvvm2026.presentation.login.model.account.AccountUiState
import com.example.mvvm2026.presentation.login.model.login.LoginAction
import com.example.mvvm2026.presentation.login.model.login.LoginEffect
import com.example.mvvm2026.presentation.login.model.login.LoginStatus
import com.example.mvvm2026.presentation.login.model.login.LoginUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds

/**
 * Coordinates form actions, domain validation, and repository authentication for either renderer.
 * Field errors and dialogs stay in StateFlow; temporary Snackbar messages use the non-replaying
 * SharedFlow. Effect emission suspends when an active collector fills the buffer; without a collector,
 * messages are discarded because replay is zero. Domain results are mapped into presentation models.
 * A separate account flow loads the sample accounts in viewModelScope. The activity runs its notice
 * countdown only while resumed, so remaining time survives backgrounding and renderer changes.
 * Accessibility can extend the initial duration. Dismissal hides the notice without clearing accounts.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
class MainViewModel(
    private val repository: LoginRepository,
    private val validateCredentials: ValidateCredentials
) : ViewModel() {
    val uiState: StateFlow<LoginUiState>
        field = MutableStateFlow(LoginUiState())

    val effects: SharedFlow<LoginEffect>
        field = MutableSharedFlow<LoginEffect>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.SUSPEND
        )
    val accountInfo: StateFlow<AccountUiState>
        field = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    private var loginJob: Job? = null
    private val accountNoticeCountdownMutex = Mutex()
    private var accountNoticeCountdownStarted = false

    init {
        viewModelScope.launch {
            when (val result = repository.getAccountInfo(SAMPLE_ACCOUNT_IDS)) {
                is AccountResult.Success -> {
                    accountInfo.value = AccountUiState.Loaded(result.accounts.map { it.toUiModel() })
                }
                AccountResult.Failed -> accountInfo.value = AccountUiState.Failure
            }
        }
    }

    fun dismissAccountNotice() {
        accountInfo.update { state ->
            if (state is AccountUiState.Loaded) state.copy(dismissalSecondsRemaining = 0) else state
        }
    }

    suspend fun runAccountNoticeCountdown(minimumDisplaySeconds: Int = 0) {
        accountNoticeCountdownMutex.withLock {
            val loaded = accountInfo.first { it != AccountUiState.Loading } as? AccountUiState.Loaded ?: return
            if (!loaded.isNoticeVisible) return
            if (!accountNoticeCountdownStarted) {
                accountNoticeCountdownStarted = true
                accountInfo.value = loaded.copy(
                    dismissalSecondsRemaining = maxOf(loaded.dismissalSecondsRemaining, minimumDisplaySeconds)
                )
            }
            while ((accountInfo.value as? AccountUiState.Loaded)?.isNoticeVisible == true) {
                delay(1.seconds)
                accountInfo.update { state ->
                    if (state is AccountUiState.Loaded && state.isNoticeVisible) {
                        state.copy(dismissalSecondsRemaining = state.dismissalSecondsRemaining - 1)
                    } else state
                }
            }
        }
    }

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.UsernameChanged -> uiState.update {
                it.copy(
                    username = action.value,
                    usernameError = if (it.validationAttempted) validateCredentials.usernameError(action.value)?.toUiError() else null
                )
            }
            is LoginAction.PasswordChanged -> uiState.update {
                it.copy(
                    password = action.value,
                    passwordError = if (it.validationAttempted) validateCredentials.passwordError(action.value)?.toUiError() else null
                )
            }
            LoginAction.Submit -> login()
            LoginAction.RequestClear -> uiState.update { it.copy(confirmClear = true) }
            LoginAction.CancelClear -> uiState.update { it.copy(confirmClear = false) }
            LoginAction.ConfirmClear -> {
                clear()
                viewModelScope.launch { effects.emit(LoginEffect.FormCleared) }
            }
            LoginAction.DismissResult -> clear()
        }
    }

    private fun login() {
        val state = uiState.value
        if (state.status == LoginStatus.Loading || state.dialog != null) return
        val credentials = Login(state.username, state.password)
        val validation = validateCredentials(credentials)
        uiState.update {
            it.copy(
                validationAttempted = true,
                usernameError = validation.usernameError?.toUiError(),
                passwordError = validation.passwordError?.toUiError()
            )
        }
        if (!validation.isValid) {
            viewModelScope.launch { effects.emit(LoginEffect.ValidationFailed) }
            return
        }
        uiState.update { it.copy(status = LoginStatus.Loading) }
        loginJob = viewModelScope.launch {
            when (val result = repository.login(credentials)) {
                LoginResult.Success -> uiState.update { it.copy(status = LoginStatus.Success) }
                LoginResult.Rejected -> uiState.update { it.copy(status = LoginStatus.Failure) }
                is LoginResult.Invalid -> {
                    uiState.update {
                        it.copy(
                            status = LoginStatus.Idle,
                            usernameError = result.validation.usernameError?.toUiError(),
                            passwordError = result.validation.passwordError?.toUiError()
                        )
                    }
                    effects.emit(LoginEffect.ValidationFailed)
                }
            }
        }
    }

    private fun clear() {
        loginJob?.cancel()
        loginJob = null
        uiState.value = LoginUiState()
    }

    private companion object {
        val SAMPLE_ACCOUNT_IDS = (1..6).map { "#$it" }
    }
}
