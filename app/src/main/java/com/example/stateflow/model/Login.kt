/**
 * Captures the credentials for one simulated login attempt.
 *
 * The ViewModel creates this value before launching the delayed check. Later changes to the form
 * therefore do not change the credentials being checked by that coroutine. This is an in-memory
 * request value; success and failure are represented separately by LoginStatus.
 */
package com.example.stateflow.model

data class Login(
    val username: String,
    val password: String
)