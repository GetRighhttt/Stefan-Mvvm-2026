package com.example.mvvm2026.domain.model.account

/**
 * Describes account information available to business logic.
 * Data records are mapped into this value; authentication secrets are not part of account information.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
data class Account(
    val id: String,
    val name: String
)
