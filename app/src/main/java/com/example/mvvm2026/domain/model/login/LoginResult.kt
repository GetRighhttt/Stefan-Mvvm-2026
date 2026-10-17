package com.example.mvvm2026.domain.model.login

import com.example.mvvm2026.domain.model.credential.CredentialValidation

/**
 * Groups the possible authentication outcomes and their related cases in one hierarchy.
 * Invalid includes field errors; Rejected means validly formatted credentials did not match an account.
 * Success is distinct from presentation status so the domain does not depend on a screen model.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
sealed interface LoginResult {
    data class Invalid(val validation: CredentialValidation) : LoginResult
    data object Success : LoginResult
    data object Rejected : LoginResult
}
