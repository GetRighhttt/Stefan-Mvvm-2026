/**
 * Uses a child DialogFragment to give the XML example the same state-driven dialogs as Compose.
 *
 * `newInstance()` puts the dialog kind in arguments so FragmentManager can recreate it.
 * `onCreateDialog()` maps that kind to resources and translates button presses into shared actions.
 * `onCancel()` distinguishes cancelling clear, which keeps the form, from dismissing a result, which
 * resets it. Both paths use the same ViewModel actions as the Compose AlertDialog.
 *
 * `finish()` obtains the activity's ViewModel before removing the fragment, then sends the action.
 * Looking it up after removal could fail because the fragment is no longer attached to its activity.
 * XmlLoginFragment compares this dialog's `kind` with the latest state before displaying another one.
 */
package com.example.stateflow

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.stateflow.model.LoginAction
import com.example.stateflow.model.LoginDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoginDialogFragment : DialogFragment() {
    private val viewModel: MainViewModel by activityViewModels()
    val kind: LoginDialog
        get() = LoginDialog.valueOf(requireArguments().getString(ARG_KIND)!!)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val confirmation = kind == LoginDialog.ClearConfirmation
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (confirmation) R.string.clear else R.string.login_dialog_title)
            .setMessage(kind.messageResource)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                finish(if (confirmation) LoginAction.ConfirmClear else LoginAction.DismissResult)
            }
            .apply {
                if (confirmation) setNegativeButton(android.R.string.cancel) { _, _ ->
                    finish(LoginAction.CancelClear)
                }
            }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        finish(if (kind == LoginDialog.ClearConfirmation) LoginAction.CancelClear else LoginAction.DismissResult)
    }

    private fun finish(action: LoginAction) {
        val model = viewModel
        dismissNow()
        model.onAction(action)
    }

    companion object {
        private const val ARG_KIND = "kind"

        fun newInstance(kind: LoginDialog) = LoginDialogFragment().apply {
            arguments = Bundle().apply { putString(ARG_KIND, kind.name) }
        }
    }
}
