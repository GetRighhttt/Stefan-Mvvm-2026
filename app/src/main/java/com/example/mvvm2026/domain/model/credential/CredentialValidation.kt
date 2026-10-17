package com.example.mvvm2026.domain.model.credential

/**
 * Returns both field errors from one validation pass.
 * The form can show every failing field together; isValid is derived from those errors rather than
 * stored as a second value that could disagree with them.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
data class CredentialValidation(
    val usernameError: UsernameError? = null,
    val passwordError: PasswordError? = null
) {
    val isValid: Boolean get() = usernameError == null && passwordError == null
}
