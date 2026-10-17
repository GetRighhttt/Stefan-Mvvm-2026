package com.example.mvvm2026.presentation.login.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.mvvm2026.R
import com.example.mvvm2026.presentation.login.MainViewModel
import com.example.mvvm2026.presentation.login.effectMessage
import com.example.mvvm2026.presentation.login.messageResource
import com.example.mvvm2026.presentation.login.model.account.AccountUiState
import com.example.mvvm2026.presentation.login.model.login.LoginAction
import com.example.mvvm2026.presentation.login.model.login.LoginDialog
import com.example.mvvm2026.presentation.login.model.login.LoginStatus
import com.example.mvvm2026.presentation.login.model.login.LoginUiState
import com.example.mvvm2026.presentation.login.passwordErrorText
import com.example.mvvm2026.presentation.login.passwordRequirements
import com.example.mvvm2026.presentation.login.usernameErrorText
import com.example.mvvm2026.presentation.login.usernameRequirements

/**
 * Collects shared state and temporary effects while the fragment view is resumed, then renders the form.
 * Lifecycle collection gives the active renderer ownership of temporary messages. Field requirements,
 * errors, progress, and dialogs come from shared state; password visibility and scrolling stay local.
 * Actions return to the ViewModel for validation and authentication. The account card renders its
 * loading, success countdown, or failure state. Its dismissal belongs to the shared ViewModel.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
@Composable
fun LoginRoute(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val accountInfo by viewModel.accountInfo.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val resources = LocalResources.current

    LaunchedEffect(viewModel, lifecycleOwner, resources) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.effects.collect { effect ->
                snackbarHostState.showSnackbar(resources.effectMessage(effect))
            }
        }
    }
    LoginScreen(state, accountInfo, viewModel::onAction, viewModel::dismissAccountNotice, snackbarHostState)
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    accountInfo: AccountUiState,
    onAction: (LoginAction) -> Unit,
    onDismissAccounts: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var showPassword by remember { mutableStateOf(false) }
    val loading = state.status == LoginStatus.Loading
    val resources = LocalResources.current
    val focusManager = LocalFocusManager.current
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface
    )
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 520.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    stringResource(R.string.login_heading),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    modifier = Modifier.semantics { heading() }
                )
                Text(stringResource(R.string.screen_subtitle), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(16.dp))
                SampleCredentialsCard()
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { onAction(LoginAction.UsernameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.username)) },
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    leadingIcon = { Icon(painterResource(R.drawable.baseline_person_24), contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = state.usernameError != null,
                    supportingText = {
                        Text(resources.usernameErrorText(state.usernameError) ?: resources.usernameRequirements)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    enabled = !loading
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.password)) },
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    leadingIcon = { Icon(painterResource(R.drawable.baseline_lock_24), contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = state.passwordError != null,
                    supportingText = {
                        Text(resources.passwordErrorText(state.passwordError) ?: resources.passwordRequirements)
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        onAction(LoginAction.Submit)
                    }),
                    singleLine = true,
                    enabled = !loading,
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }, enabled = !loading) {
                            Icon(
                                painterResource(if (showPassword) R.drawable.ic_visibility_off_24 else R.drawable.ic_visibility_24),
                                contentDescription = stringResource(if (showPassword) R.string.hide_password else R.string.show_password)
                            )
                        }
                    }
                )
                if (loading) {
                    Spacer(Modifier.height(24.dp))
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { onAction(LoginAction.RequestClear) },
                        modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp)) {
                        Text(stringResource(R.string.clear))
                    }
                    Button(onClick = {
                        focusManager.clearFocus()
                        onAction(LoginAction.Submit)
                    }, enabled = !loading,
                        modifier = Modifier.weight(2f).height(52.dp), shape = RoundedCornerShape(14.dp)) {
                        Text(stringResource(R.string.login))
                    }
                }
                AnimatedVisibility(
                    visible = accountInfo !is AccountUiState.Loaded || accountInfo.isNoticeVisible,
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(Modifier.padding(top = 16.dp)) {
                        AccountInfoCard(accountInfo, onDismissAccounts)
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
