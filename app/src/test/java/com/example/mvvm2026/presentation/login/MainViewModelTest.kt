package com.example.mvvm2026.presentation.login

import com.example.mvvm2026.data.repository.LoginRepositoryImpl
import com.example.mvvm2026.data.sourceimpl.AccountDataSourceImpl
import com.example.mvvm2026.domain.model.account.AccountResult
import com.example.mvvm2026.domain.model.login.Login
import com.example.mvvm2026.domain.model.login.LoginResult
import com.example.mvvm2026.domain.repository.LoginRepository
import com.example.mvvm2026.domain.usecase.ValidateCredentials
import com.example.mvvm2026.presentation.login.model.account.AccountUiModel
import com.example.mvvm2026.presentation.login.model.account.AccountUiState
import com.example.mvvm2026.presentation.login.model.login.LoginAction
import com.example.mvvm2026.presentation.login.model.login.LoginDialog
import com.example.mvvm2026.presentation.login.model.login.LoginEffect
import com.example.mvvm2026.presentation.login.model.login.LoginStatus
import com.example.mvvm2026.presentation.login.model.credential.PasswordFieldError
import com.example.mvvm2026.presentation.login.model.credential.UsernameFieldError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Checks state, effect delivery, validation recovery, and cancellation with virtual time.
 * Account notice tests cover foreground time, accessibility duration, and dismissal without data loss.
 * The injected recording repository proves that invalid input never starts authentication work.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts idle`() {
        assertEquals(LoginStatus.Idle, createViewModel().uiState.value.status)
    }

    @Test
    fun `correct credentials load for one second before succeeding`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.login("username", "Password1!")
        assertEquals(LoginStatus.Loading, viewModel.uiState.value.status)
        runCurrent()
        advanceTimeBy(999)
        assertEquals(LoginStatus.Loading, viewModel.uiState.value.status)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(LoginStatus.Success, viewModel.uiState.value.status)
    }

    @Test
    fun `incorrect credentials fail`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.login("username", "WrongPass1!")
        advanceUntilIdle()
        assertEquals(LoginStatus.Failure, viewModel.uiState.value.status)
    }

    @Test
    fun `blank credentials stay idle`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.login("", "")
        advanceUntilIdle()
        assertEquals(LoginStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `another tap while loading does not replace the attempt`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.login("username", "Password1!")
        viewModel.login("other_user", "WrongPass1!")
        advanceUntilIdle()
        assertEquals(LoginStatus.Success, viewModel.uiState.value.status)
    }

    @Test
    fun `clearing during login cancels the pending result`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.login("username", "Password1!")
        runCurrent()
        advanceTimeBy(500)
        viewModel.clear()
        advanceUntilIdle()
        assertEquals(LoginStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `a new attempt works after clearing`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.login("other_user", "WrongPass1!")
        runCurrent()
        viewModel.clear()
        viewModel.login("username", "Password1!")
        advanceUntilIdle()
        assertEquals(LoginStatus.Success, viewModel.uiState.value.status)
        viewModel.clear()
        assertEquals(LoginStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `blank submission emits a validation message to an active collector`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<LoginEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.toList(effects) }
        viewModel.onAction(LoginAction.Submit)
        runCurrent()
        assertEquals(listOf(LoginEffect.ValidationFailed), effects)
        assertEquals(LoginStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `confirmed clear empties inputs and emits one message`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<LoginEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.toList(effects) }
        viewModel.login("username", "Password1!")
        viewModel.onAction(LoginAction.ConfirmClear)
        advanceUntilIdle()
        assertEquals("", viewModel.uiState.value.username)
        assertEquals("", viewModel.uiState.value.password)
        assertEquals(listOf(LoginEffect.FormCleared), effects)
    }

    @Test
    fun `messages emitted without a screen are not replayed to a new collector`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.onAction(LoginAction.Submit)
        runCurrent()
        val effects = mutableListOf<LoginEffect>()
        val firstCollector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }
        assertEquals(emptyList<LoginEffect>(), effects)
        viewModel.onAction(LoginAction.Submit)
        runCurrent()
        assertEquals(listOf(LoginEffect.ValidationFailed), effects)
        firstCollector.cancel()
        val nextEffects = mutableListOf<LoginEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.toList(nextEffects) }
        assertEquals(emptyList<LoginEffect>(), nextEffects)
    }

    @Test
    fun `a slow collector receives buffered and suspended messages in order`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<LoginEffect>()
        val releaseCollector = CompletableDeferred<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effect ->
                effects += effect
                if (effects.size == 1) releaseCollector.await()
            }
        }

        viewModel.onAction(LoginAction.Submit)
        runCurrent()
        viewModel.onAction(LoginAction.ConfirmClear)
        runCurrent()
        viewModel.onAction(LoginAction.Submit)
        runCurrent()
        assertEquals(listOf(LoginEffect.ValidationFailed), effects)

        releaseCollector.complete(Unit)
        runCurrent()
        assertEquals(listOf(LoginEffect.ValidationFailed, LoginEffect.FormCleared, LoginEffect.ValidationFailed), effects)
    }

    @Test
    fun `cancelling clear reveals the result that arrived behind confirmation`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.login("username", "Password1!")
        viewModel.onAction(LoginAction.RequestClear)
        advanceUntilIdle()
        assertEquals(LoginStatus.Success, viewModel.uiState.value.status)
        assertEquals(LoginDialog.ClearConfirmation, viewModel.uiState.value.dialog)
        viewModel.onAction(LoginAction.CancelClear)
        assertEquals(LoginDialog.Success, viewModel.uiState.value.dialog)
        assertEquals("username", viewModel.uiState.value.username)
    }

    @Test
    fun `invalid input stays idle with field errors and never calls the repository`() = runTest(dispatcher) {
        var calls = 0
        val repository = object : LoginRepository {
            override suspend fun getAccountInfo(accountIds: List<String>): AccountResult = AccountResult.Success(emptyList())
            override suspend fun login(credentials: Login): LoginResult {
                calls++
                return LoginResult.Success
            }
        }
        val viewModel = createViewModel(repository = repository)
        viewModel.login("ab", "short")
        advanceUntilIdle()
        assertEquals(0, calls)
        assertEquals(LoginStatus.Idle, viewModel.uiState.value.status)
        assertEquals(UsernameFieldError.Length, viewModel.uiState.value.usernameError)
        assertEquals(PasswordFieldError.Length, viewModel.uiState.value.passwordError)
    }

    @Test
    fun `field errors appear after submission and clear as each field becomes valid`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.onAction(LoginAction.UsernameChanged("ab"))
        assertEquals(null, viewModel.uiState.value.usernameError)
        viewModel.onAction(LoginAction.Submit)
        assertEquals(UsernameFieldError.Length, viewModel.uiState.value.usernameError)
        assertEquals(PasswordFieldError.Required, viewModel.uiState.value.passwordError)
        viewModel.onAction(LoginAction.UsernameChanged("username"))
        assertEquals(null, viewModel.uiState.value.usernameError)
        assertEquals(PasswordFieldError.Required, viewModel.uiState.value.passwordError)
        viewModel.onAction(LoginAction.PasswordChanged("Password1!"))
        assertEquals(null, viewModel.uiState.value.passwordError)
        viewModel.onAction(LoginAction.Submit)
        advanceUntilIdle()
        assertEquals(LoginStatus.Success, viewModel.uiState.value.status)
    }

    @Test
    fun `sample account lookup becomes presentation state and survives clearing the form`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<LoginEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.toList(effects) }
        assertEquals(AccountUiState.Loading, viewModel.accountInfo.value)
        advanceTimeBy(3999)
        assertEquals(AccountUiState.Loading, viewModel.accountInfo.value)
        advanceTimeBy(1)
        runCurrent()
        val expected = AccountUiState.Loaded((1..6).map { AccountUiModel("#$it", "Sample account $it") })
        assertEquals(expected, viewModel.accountInfo.value)
        assertEquals(emptyList<LoginEffect>(), effects)
        viewModel.clear()
        runCurrent()
        assertEquals(expected, viewModel.accountInfo.value)
        assertEquals(listOf(LoginEffect.FormCleared), effects)
    }

    @Test
    fun `success notice starts after loading and dismisses after eight visible seconds`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.runAccountNoticeCountdown() }
        advanceTimeBy(4000)
        runCurrent()
        val loaded = viewModel.accountInfo.value as AccountUiState.Loaded
        assertEquals(8, loaded.dismissalSecondsRemaining)

        advanceTimeBy(1000)
        runCurrent()
        assertEquals(loaded.copy(dismissalSecondsRemaining = 7), viewModel.accountInfo.value)
        advanceTimeBy(6999)
        runCurrent()
        assertEquals(loaded.copy(dismissalSecondsRemaining = 1), viewModel.accountInfo.value)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(loaded.copy(dismissalSecondsRemaining = 0), viewModel.accountInfo.value)
    }

    @Test
    fun `loading while backgrounded leaves the full notice duration for resume`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceTimeBy(60_000)
        runCurrent()
        val loaded = viewModel.accountInfo.value as AccountUiState.Loaded
        assertEquals(8, loaded.dismissalSecondsRemaining)

        backgroundScope.launch { viewModel.runAccountNoticeCountdown() }
        runCurrent()
        assertEquals(loaded, viewModel.accountInfo.value)
        advanceTimeBy(1000)
        runCurrent()
        assertEquals(loaded.copy(dismissalSecondsRemaining = 7), viewModel.accountInfo.value)
    }

    @Test
    fun `manual dismissal keeps accounts and cannot be undone by a pending tick or new timer`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.runAccountNoticeCountdown() }
        advanceTimeBy(4500)
        runCurrent()
        val loaded = viewModel.accountInfo.value as AccountUiState.Loaded
        viewModel.dismissAccountNotice()
        val dismissed = loaded.copy(dismissalSecondsRemaining = 0)
        assertEquals(dismissed, viewModel.accountInfo.value)

        advanceTimeBy(1000)
        runCurrent()
        backgroundScope.launch { viewModel.runAccountNoticeCountdown(minimumDisplaySeconds = 20) }
        runCurrent()
        assertEquals(dismissed, viewModel.accountInfo.value)
    }

    @Test
    fun `notice countdown pauses on cancellation and resumes without restarting its duration`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val countdown = backgroundScope.launch {
            viewModel.runAccountNoticeCountdown(minimumDisplaySeconds = 12)
        }
        advanceTimeBy(4000)
        runCurrent()
        val loaded = viewModel.accountInfo.value as AccountUiState.Loaded
        assertEquals(12, loaded.dismissalSecondsRemaining)
        advanceTimeBy(2400)
        runCurrent()
        countdown.cancelAndJoin()
        val paused = loaded.copy(dismissalSecondsRemaining = 10)
        assertEquals(paused, viewModel.accountInfo.value)

        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(paused, viewModel.accountInfo.value)
        backgroundScope.launch { viewModel.runAccountNoticeCountdown(minimumDisplaySeconds = 20) }
        runCurrent()
        assertEquals(paused, viewModel.accountInfo.value)
        advanceTimeBy(10_000)
        runCurrent()
        assertEquals(loaded.copy(dismissalSecondsRemaining = 0), viewModel.accountInfo.value)
    }

    @Test
    fun `duplicate countdown callers do not dismiss the notice sooner`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        repeat(2) {
            backgroundScope.launch { viewModel.runAccountNoticeCountdown() }
        }
        advanceTimeBy(6000)
        runCurrent()
        val loaded = viewModel.accountInfo.value as AccountUiState.Loaded
        assertEquals(6, loaded.dismissalSecondsRemaining)
        advanceTimeBy(6000)
        runCurrent()
        assertEquals(loaded.copy(dismissalSecondsRemaining = 0), viewModel.accountInfo.value)
    }

    @Test
    fun `domain account failure becomes presentation failure`() = runTest(dispatcher) {
        val repository = object : LoginRepository {
            override suspend fun getAccountInfo(accountIds: List<String>): AccountResult = AccountResult.Failed
            override suspend fun login(credentials: Login): LoginResult = LoginResult.Rejected
        }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        assertEquals(AccountUiState.Failure, viewModel.accountInfo.value)
    }

    private fun createViewModel(
        repository: LoginRepository = LoginRepositoryImpl(AccountDataSourceImpl(), ValidateCredentials())
    ) =
        MainViewModel(repository, ValidateCredentials())

    private fun MainViewModel.login(username: String, password: String) {
        onAction(LoginAction.UsernameChanged(username))
        onAction(LoginAction.PasswordChanged(password))
        onAction(LoginAction.Submit)
    }

    private fun MainViewModel.clear() = onAction(LoginAction.ConfirmClear)

}
