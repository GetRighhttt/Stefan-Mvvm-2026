package com.example.mvvm2026.presentation.login.model.login

/**
 * Groups temporary messages emitted to the active renderer.
 * ValidationFailed points to durable field errors already in state. FormCleared confirms an explicit
 * clear action. Account completion and dismissal belong to the persistent account state.
 * These messages do not replay when a new screen starts collecting.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
sealed interface LoginEffect {
    data object ValidationFailed : LoginEffect
    data object FormCleared : LoginEffect
}
