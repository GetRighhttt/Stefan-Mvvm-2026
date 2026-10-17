package com.example.mvvm2026.data.model.account

/**
 * Carries simulated account fields in the data layer.
 * The repository maps records to domain accounts before returning them to callers.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
data class AccountRecord(
    val id: String,
    val name: String
)
