/**
 * Names the temporary messages sent from the ViewModel to the active renderer.
 *
 * MissingCredentials asks for both inputs before an attempt starts. FormCleared confirms an explicit
 * clear action. Either screen maps these values to the same Snackbar text through LoginResources.kt.
 *
 * The SharedFlow carrying them has no replay. A message can be lost while no screen is collecting,
 * so a login result or dialog that must survive a renderer change belongs in LoginUiState instead.
 */
package com.example.stateflow.model

sealed interface LoginEffect {
    data object MissingCredentials : LoginEffect
    data object FormCleared : LoginEffect
}
