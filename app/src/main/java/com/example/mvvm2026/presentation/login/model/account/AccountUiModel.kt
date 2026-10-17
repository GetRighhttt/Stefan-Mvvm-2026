package com.example.mvvm2026.presentation.login.model.account

/**
 * Holds account fields prepared for presentation.
 * Views consume this model rather than importing domain accounts or simulated data records.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
data class AccountUiModel(
    val id: String,
    val displayName: String
)
