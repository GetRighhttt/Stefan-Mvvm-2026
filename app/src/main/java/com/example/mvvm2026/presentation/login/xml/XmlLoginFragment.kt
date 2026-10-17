package com.example.mvvm2026.presentation.login.xml

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mvvm2026.R
import com.example.mvvm2026.databinding.FragmentLoginBinding
import com.example.mvvm2026.presentation.login.model.account.AccountUiState
import com.example.mvvm2026.presentation.login.MainViewModel
import com.example.mvvm2026.presentation.login.effectMessage
import com.example.mvvm2026.presentation.login.model.login.LoginAction
import com.example.mvvm2026.presentation.login.model.login.LoginStatus
import com.example.mvvm2026.presentation.login.model.login.LoginUiState
import com.example.mvvm2026.presentation.login.passwordErrorText
import com.example.mvvm2026.presentation.login.passwordRequirements
import com.example.mvvm2026.presentation.login.usernameErrorText
import com.example.mvvm2026.presentation.login.usernameRequirements
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Renders shared state through View Binding and collects effects for the current view lifecycle.
 * TextInputLayout owns requirement and error text; the child DialogFragment follows the same dialog
 * state used by Compose. The account card collects its own request state; replacing the view cancels
 * all collectors without restarting the shared requests. The success card displays the ViewModel's
 * dismissal countdown and forwards manual dismissal without owning a separate timer.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
class XmlLoginFragment : Fragment(R.layout.fragment_login) {
    private val viewModel: MainViewModel by activityViewModels()
    private var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentLoginBinding.bind(view)
        ViewCompat.setAccessibilityHeading(binding.loginHeading, true)
        ViewCompat.setAccessibilityHeading(binding.accountHeading, true)
        binding.usernameLayout.helperText = resources.usernameRequirements
        binding.passwordLayout.helperText = resources.passwordRequirements
        binding.etLogin.doAfterTextChanged {
            viewModel.onAction(LoginAction.UsernameChanged(it.toString()))
        }
        binding.etPassword.doAfterTextChanged {
            viewModel.onAction(LoginAction.PasswordChanged(it.toString()))
        }
        binding.btnLogin.setOnClickListener { submit(binding) }
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit(binding)
                true
            } else {
                false
            }
        }
        binding.btnClear.setOnClickListener { viewModel.onAction(LoginAction.RequestClear) }
        binding.btnDismissAccount.setOnClickListener { viewModel.dismissAccountNotice() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch { viewModel.uiState.collect { renderState(binding, it) } }
                launch { viewModel.accountInfo.collect { renderAccountInfo(binding, it) } }
                launch {
                    viewModel.effects.collect { effect ->
                        snackbar?.dismiss()
                        snackbar = Snackbar.make(view, resources.effectMessage(effect), Snackbar.LENGTH_SHORT)
                            .also { it.show() }
                    }
                }
            }
        }
    }

    private fun submit(binding: FragmentLoginBinding) {
        WindowInsetsControllerCompat(requireActivity().window, binding.root)
            .hide(WindowInsetsCompat.Type.ime())
        binding.etLogin.clearFocus()
        binding.etPassword.clearFocus()
        viewModel.onAction(LoginAction.Submit)
    }

    private fun renderState(binding: FragmentLoginBinding, state: LoginUiState) {
        if (binding.etLogin.text.toString() != state.username) binding.etLogin.setText(state.username)
        if (binding.etPassword.text.toString() != state.password) binding.etPassword.setText(state.password)
        binding.usernameLayout.error = resources.usernameErrorText(state.usernameError)
        binding.passwordLayout.error = resources.passwordErrorText(state.passwordError)
        val loading = state.status == LoginStatus.Loading
        binding.progressBar.isVisible = loading
        binding.btnLogin.isEnabled = !loading
        binding.etLogin.isEnabled = !loading
        binding.etPassword.isEnabled = !loading

        val existing = childFragmentManager.findFragmentByTag(DIALOG_TAG) as? LoginDialogFragment
        if (existing?.kind == state.dialog) return
        existing?.dismissNow()
        state.dialog?.let { LoginDialogFragment.newInstance(it).showNow(childFragmentManager, DIALOG_TAG) }
    }

    private fun renderAccountInfo(binding: FragmentLoginBinding, state: AccountUiState) {
        val loaded = state as? AccountUiState.Loaded
        binding.accountCard.isVisible = loaded?.isNoticeVisible ?: true
        binding.accountProgress.isVisible = state == AccountUiState.Loading
        binding.accountDismissRow.isVisible = loaded?.isNoticeVisible == true
        binding.accountHeading.setText(
            when (state) {
                AccountUiState.Loading -> R.string.account_info_title
                is AccountUiState.Loaded -> R.string.account_info_success_title
                AccountUiState.Failure -> R.string.account_info_failure_title
            },
        )
        val message = when (state) {
            AccountUiState.Loading -> getString(R.string.account_info_loading)
            is AccountUiState.Loaded -> resources.getQuantityString(
                R.plurals.accounts_loaded,
                state.accounts.size,
                state.accounts.size,
            )
            AccountUiState.Failure -> getString(R.string.account_info_failure)
        }
        if (binding.accountInfo.text.toString() != message) binding.accountInfo.text = message
        loaded?.let {
            binding.accountCountdown.text = resources.getQuantityString(
                R.plurals.account_info_dismiss_countdown,
                it.dismissalSecondsRemaining,
                it.dismissalSecondsRemaining,
            )
        }
        val context = binding.root.context
        val success = loaded != null
        val errorColor = MaterialColors.getColor(binding.root, androidx.appcompat.R.attr.colorError)
        val foreground = when (state) {
            AccountUiState.Loading -> ContextCompat.getColor(context, R.color.surface_text)
            is AccountUiState.Loaded -> ContextCompat.getColor(context, R.color.on_success_container)
            AccountUiState.Failure -> errorColor
        }
        binding.accountCard.setCardBackgroundColor(
            ContextCompat.getColor(context, if (success) R.color.success_container else R.color.surface_card),
        )
        binding.accountCard.strokeColor = ContextCompat.getColor(
            context,
            if (success) R.color.success_container else R.color.surface_outline_variant,
        )
        binding.accountBadge.setCardBackgroundColor(
            ContextCompat.getColor(context, if (success) R.color.surface_card else R.color.xml_secondary_container),
        )
        binding.accountHeading.setTextColor(foreground)
        binding.accountInfo.setTextColor(
            if (state == AccountUiState.Loading) ContextCompat.getColor(context, R.color.surface_muted) else foreground,
        )
        binding.accountIcon.setImageResource(
            when (state) {
                AccountUiState.Loading -> R.drawable.ic_accounts_24
                is AccountUiState.Loaded -> R.drawable.ic_check_24
                AccountUiState.Failure -> R.drawable.ic_error_24
            },
        )
        binding.accountIcon.imageTintList = ColorStateList.valueOf(
            when (state) {
                AccountUiState.Loading -> ContextCompat.getColor(context, R.color.xml_primary)
                is AccountUiState.Loaded -> foreground
                AccountUiState.Failure -> errorColor
            },
        )
    }

    override fun onDestroyView() {
        snackbar?.dismiss()
        snackbar = null
        super.onDestroyView()
    }

    companion object {
        private const val DIALOG_TAG = "login_dialog"
    }
}
