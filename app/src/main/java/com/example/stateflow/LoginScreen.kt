/**
 * Separates Compose flow collection from rendering so the same contract can be compared with XML.
 *
 * `LoginRoute()` reads `uiState` with `collectAsStateWithLifecycle()` and collects `effects` in a
 * LaunchedEffect tied to the view lifecycle. Both collect while RESUMED. State emissions request
 * recomposition; effects call SnackbarHostState and are not stored as screen state. LocalResources
 * provides messages that follow the current configuration.
 *
 * `LoginScreen()` receives state and an action callback. It renders the form, loading controls, and
 * AlertDialog without looking up a ViewModel. Password visibility is local UI state; credentials and
 * dialog decisions belong to the shared ViewModel. The dialog is derived from state, so a result stays
 * available when the screen is recreated or the renderer changes.
 *
 * Scaffold uses zero window insets because the activity already applies system-bar, cutout, and
 * keyboard padding to the parent shared by both examples.
 */
package com.example.stateflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.stateflow.model.LoginAction
import com.example.stateflow.model.LoginDialog
import com.example.stateflow.model.LoginStatus
import com.example.stateflow.model.LoginUiState

@Composable
fun LoginRoute(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val resources = LocalResources.current

    LaunchedEffect(viewModel, lifecycleOwner, resources) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.effects.collect { effect ->
                snackbarHostState.showSnackbar(resources.getString(effect.messageResource))
            }
        }
    }
    LoginScreen(state, viewModel::onAction, snackbarHostState)
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var showPassword by remember { mutableStateOf(false) }
    val loading = state.status == LoginStatus.Loading
    // The activity applies system-bar and keyboard insets to both examples.
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, contentWindowInsets = WindowInsets(0)) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.login_stateflow_example),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(32.dp))
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { onAction(LoginAction.UsernameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.login)) },
                    supportingText = { Text(stringResource(R.string.login_helper)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    enabled = !loading
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.password)) },
                    supportingText = { Text(stringResource(R.string.password_helper)) },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    singleLine = true,
                    enabled = !loading,
                    trailingIcon = {
                        TextButton(onClick = { showPassword = !showPassword }) {
                            Text(stringResource(if (showPassword) R.string.hide_password else R.string.show_password))
                        }
                    }
                )
                if (loading) {
                    Spacer(Modifier.height(24.dp))
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)) {
                    OutlinedButton(onClick = { onAction(LoginAction.RequestClear) }) {
                        Text(stringResource(R.string.clear))
                    }
                    Button(onClick = { onAction(LoginAction.Submit) }, enabled = !loading) {
                        Text(stringResource(R.string.login))
                    }
                }
            }
        }
    }
    state.dialog?.let { dialog ->
        val confirmation = dialog == LoginDialog.ClearConfirmation
        val dismissAction = if (confirmation) LoginAction.CancelClear else LoginAction.DismissResult
        AlertDialog(
            onDismissRequest = { onAction(dismissAction) },
            title = { Text(stringResource(if (confirmation) R.string.clear else R.string.login_dialog_title)) },
            text = { Text(stringResource(dialog.messageResource)) },
            confirmButton = {
                TextButton(onClick = {
                    onAction(if (confirmation) LoginAction.ConfirmClear else LoginAction.DismissResult)
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = if (confirmation) {
                { TextButton(onClick = { onAction(LoginAction.CancelClear) }) { Text(stringResource(android.R.string.cancel)) } }
            } else null
        )
    }
}
