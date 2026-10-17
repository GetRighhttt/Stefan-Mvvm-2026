package com.example.mvvm2026.presentation.login.model.account

/**
 * Describes the sample account request independently of the login form.
 * Loaded accounts include the remaining visible time for their success notice. Dismissing the
 * notice sets that time to zero; the account data stays available to either renderer.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
sealed interface AccountUiState {
    data object Loading : AccountUiState
    data class Loaded(
        val accounts: List<AccountUiModel>,
        val dismissalSecondsRemaining: Int = 8
    ) : AccountUiState {
        val isNoticeVisible: Boolean
            get() = dismissalSecondsRemaining > 0
    }
    data object Failure : AccountUiState
}
