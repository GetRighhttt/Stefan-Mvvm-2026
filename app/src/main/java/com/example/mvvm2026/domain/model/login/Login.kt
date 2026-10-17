package com.example.mvvm2026.domain.model.login

/**
 * Carries the credential snapshot passed to validation and authentication.
 * Values are checked as entered. Keeping this domain value separate from UI state lets a repository
 * accept credentials without depending on presentation models.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
data class Login(
    val username: String,
    val password: String
)
