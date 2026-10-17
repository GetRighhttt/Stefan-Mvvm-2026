/**
 * Checks the shared contract without an Android screen or a real one-second wait.
 *
 * `setUp()` replaces Dispatchers.Main with a test dispatcher before creating a ViewModel; `tearDown()`
 * restores it. The timing tests advance the shared scheduler to check loading, completion, cancellation,
 * and retry. The helper actions submit the same inputs that either UI would send.
 *
 * Effect tests start collectors with an UnconfinedTestDispatcher so they are subscribed before an
 * action emits. `backgroundScope` cancels those never-ending collectors when the test finishes. The
 * no-replay case checks messages emitted without a subscriber and a later collector joining after
 * another subscriber has received a message. Confirmation tests check that a pending result is still
 * available when clear is cancelled.
 */
package com.example.stateflow

import com.example.stateflow.model.LoginAction
import com.example.stateflow.model.LoginDialog
import com.example.stateflow.model.LoginEffect
import com.example.stateflow.model.LoginStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
        assertEquals(LoginStatus.Idle, MainViewModel().uiState.value.status)
    }

    @Test
    fun `correct credentials load for one second before succeeding`() = runTest(dispatcher) {
        val viewModel = MainViewModel()
        viewModel.login("username", "password")
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
        val viewModel = MainViewModel()
        viewModel.login("username", "wrong")
        advanceUntilIdle()
        assertEquals(LoginStatus.Failure, viewModel.uiState.value.status)
    }

    @Test
    fun `blank credentials stay idle`() = runTest(dispatcher) {
        val viewModel = MainViewModel()
        viewModel.login("", "")
        advanceUntilIdle()
        assertEquals(LoginStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `another tap while loading does not replace the attempt`() = runTest(dispatcher) {
        val viewModel = MainViewModel()
        viewModel.login("username", "password")
        viewModel.login("wrong", "wrong")
        advanceUntilIdle()
        assertEquals(LoginStatus.Success, viewModel.uiState.value.status)
    }

    @Test
    fun `clearing during login cancels the pending result`() = runTest(dispatcher) {
        val viewModel = MainViewModel()
        viewModel.login("username", "password")
        runCurrent()
        advanceTimeBy(500)
        viewModel.clear()
        advanceUntilIdle()
        assertEquals(LoginStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `a new attempt works after clearing`() = runTest(dispatcher) {
        val viewModel = MainViewModel()
        viewModel.login("wrong", "wrong")
        runCurrent()
        viewModel.clear()
        viewModel.login("username", "password")
        advanceUntilIdle()
        assertEquals(LoginStatus.Success, viewModel.uiState.value.status)
        viewModel.clear()
        assertEquals(LoginStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `blank submission emits a validation message to an active collector`() = runTest(dispatcher) {
        val viewModel = MainViewModel()
        val effects = mutableListOf<LoginEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.toList(effects) }
        viewModel.onAction(LoginAction.Submit)
        assertEquals(listOf(LoginEffect.MissingCredentials), effects)
        assertEquals(LoginStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `confirmed clear empties inputs and emits one message`() = runTest(dispatcher) {
        val viewModel = MainViewModel()
        val effects = mutableListOf<LoginEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.toList(effects) }
        viewModel.login("username", "password")
        viewModel.onAction(LoginAction.ConfirmClear)
        advanceUntilIdle()
        assertEquals("", viewModel.uiState.value.username)
        assertEquals("", viewModel.uiState.value.password)
        assertEquals(listOf(LoginEffect.FormCleared), effects)
    }

    @Test
    fun `messages emitted without a screen are not replayed to a new collector`() = runTest(dispatcher) {
        val viewModel = MainViewModel()
        viewModel.onAction(LoginAction.Submit)
        val effects = mutableListOf<LoginEffect>()
        val firstCollector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }
        assertEquals(emptyList<LoginEffect>(), effects)
        viewModel.onAction(LoginAction.Submit)
        assertEquals(listOf(LoginEffect.MissingCredentials), effects)
        firstCollector.cancel()
        val nextEffects = mutableListOf<LoginEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.toList(nextEffects) }
        assertEquals(emptyList<LoginEffect>(), nextEffects)
    }

    @Test
    fun `cancelling clear reveals the result that arrived behind confirmation`() = runTest(dispatcher) {
        val viewModel = MainViewModel()
        viewModel.login("username", "password")
        viewModel.onAction(LoginAction.RequestClear)
        advanceUntilIdle()
        assertEquals(LoginStatus.Success, viewModel.uiState.value.status)
        assertEquals(LoginDialog.ClearConfirmation, viewModel.uiState.value.dialog)
        viewModel.onAction(LoginAction.CancelClear)
        assertEquals(LoginDialog.Success, viewModel.uiState.value.dialog)
        assertEquals("username", viewModel.uiState.value.username)
    }

    private fun MainViewModel.login(username: String, password: String) {
        onAction(LoginAction.UsernameChanged(username))
        onAction(LoginAction.PasswordChanged(password))
        onAction(LoginAction.Submit)
    }

    private fun MainViewModel.clear() = onAction(LoginAction.ConfirmClear)

}
