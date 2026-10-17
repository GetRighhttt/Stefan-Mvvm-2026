/**
 * Maps UI-neutral dialog and effect values to Android string resources for both renderers.
 *
 * `LoginDialog.messageResource` selects confirmation, success, or failure text. The title and buttons
 * remain each renderer's responsibility. `LoginEffect.messageResource` selects the temporary Snackbar
 * message. Keeping the mappings together lets both UIs use the same wording without putting Android
 * resource IDs or Context references in the ViewModel and model classes.
 */
package com.example.stateflow

import androidx.annotation.StringRes
import com.example.stateflow.model.LoginDialog
import com.example.stateflow.model.LoginEffect

@get:StringRes
val LoginDialog.messageResource: Int
    get() = when (this) {
        LoginDialog.ClearConfirmation -> R.string.clear_confirmation
        LoginDialog.Success -> R.string.login_success
        LoginDialog.Failure -> R.string.login_failure
    }

@get:StringRes
val LoginEffect.messageResource: Int
    get() = when (this) {
        LoginEffect.MissingCredentials -> R.string.missing_credentials
        LoginEffect.FormCleared -> R.string.form_cleared
    }
