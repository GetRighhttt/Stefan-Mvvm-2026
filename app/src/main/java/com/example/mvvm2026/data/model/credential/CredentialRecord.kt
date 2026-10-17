package com.example.mvvm2026.data.model.credential

/**
 * Stores the fixed credentials used by the local account check.
 * This data-only record never becomes domain account information or presentation state.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
data class CredentialRecord(
    val username: String,
    val password: String
)
