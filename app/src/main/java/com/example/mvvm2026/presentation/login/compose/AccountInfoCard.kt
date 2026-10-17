package com.example.mvvm2026.presentation.login.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mvvm2026.R
import com.example.mvvm2026.presentation.login.model.account.AccountUiState

/**
 * Shows independent account progress, then a success summary with time to read or dismiss it.
 * The activity runs the shared countdown while resumed. Stable completion text is announced politely;
 * changing seconds stay outside that live region so a screen reader is not interrupted every second.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
@Composable
fun AccountInfoCard(state: AccountUiState, onDismiss: () -> Unit) {
    val loaded = state as? AccountUiState.Loaded
    val contentColor = when (state) {
        is AccountUiState.Loaded -> colorResource(R.color.on_success_container)
        AccountUiState.Failure -> MaterialTheme.colorScheme.error
        AccountUiState.Loading -> MaterialTheme.colorScheme.onSurface
    }
    val title = when (state) {
        is AccountUiState.Loaded -> R.string.account_info_success_title
        AccountUiState.Failure -> R.string.account_info_failure_title
        AccountUiState.Loading -> R.string.account_info_title
    }
    val icon = when (state) {
        is AccountUiState.Loaded -> R.drawable.ic_check_24
        AccountUiState.Failure -> R.drawable.ic_error_24
        AccountUiState.Loading -> R.drawable.ic_accounts_24
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (loaded != null) colorResource(R.color.success_container) else MaterialTheme.colorScheme.surface,
        contentColor = contentColor,
        border = if (loaded == null) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (loaded != null) colorResource(R.color.surface_card) else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(painterResource(icon), contentDescription = null,
                        tint = if (state == AccountUiState.Loading) MaterialTheme.colorScheme.primary else contentColor,
                        modifier = Modifier.padding(10.dp).size(20.dp))
                }
                Column(Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(title), style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
                    Text(
                        when (state) {
                            is AccountUiState.Loaded -> pluralStringResource(R.plurals.accounts_loaded, state.accounts.size, state.accounts.size)
                            AccountUiState.Loading -> stringResource(R.string.account_info_loading)
                            AccountUiState.Failure -> stringResource(R.string.account_info_failure)
                        }, style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (state == AccountUiState.Loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
            }
            if (loaded != null) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        pluralStringResource(R.plurals.account_info_dismiss_countdown,
                            loaded.dismissalSecondsRemaining, loaded.dismissalSecondsRemaining),
                        modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.account_info_dismiss), color = contentColor)
                    }
                }
            }
        }
    }
}
