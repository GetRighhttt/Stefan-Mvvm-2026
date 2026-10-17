package com.example.mvvm2026.presentation.login.model.login

/**
 * Groups the persistent stages of a login attempt in one sealed hierarchy.
 * Loading controls progress and input availability. Success and failure keep a result available until
 * the user acknowledges it, including across renderer changes and activity recreation.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
sealed interface LoginStatus {
    data object Idle : LoginStatus
    data object Loading : LoginStatus
    data object Success : LoginStatus
    data object Failure : LoginStatus
}
