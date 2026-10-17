package com.example.mvvm2026.presentation.login.xml

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mvvm2026.R
import com.example.mvvm2026.presentation.login.MainViewModel
import com.example.mvvm2026.presentation.login.messageResource
import com.example.mvvm2026.presentation.login.model.login.LoginAction
import com.example.mvvm2026.presentation.login.model.login.LoginDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Translates XML dialog buttons and cancellation into shared form actions.
 * The dialog kind survives fragment recreation. The ViewModel is obtained before dismissal because
 * looking it up after the fragment detaches would lose access to its activity owner.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
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
