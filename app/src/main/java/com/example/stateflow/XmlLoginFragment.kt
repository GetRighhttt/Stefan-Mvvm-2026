/**
 * Shows how a View Binding screen consumes the shared state and effect contract.
 *
 * `onViewCreated()` turns field edits and button clicks into actions. It starts separate state and
 * effect collectors inside the view's RESUMED lifecycle block, so the collectors end when this view
 * is replaced. State rendering and a temporary Snackbar do not block each other's collection.
 *
 * `renderState()` sets field text only when the value differs, so an update does not reset the cursor
 * or produce unnecessary text callbacks. It also updates loading controls and compares the requested
 * dialog with the child DialogFragment before replacing it. That comparison handles restored dialogs
 * and prevents a new dialog for every state emission.
 *
 * The binding stays local to the view setup and collection rather than outliving the view as a field.
 * `onDestroyView()` dismisses the Snackbar before the XML example is removed.
 */
package com.example.stateflow

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.stateflow.databinding.FragmentLoginBinding
import com.example.stateflow.model.LoginAction
import com.example.stateflow.model.LoginStatus
import com.example.stateflow.model.LoginUiState
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class XmlLoginFragment : Fragment(R.layout.fragment_login) {
    private val viewModel: MainViewModel by activityViewModels()
    private var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentLoginBinding.bind(view)
        ViewCompat.setAccessibilityHeading(binding.loginHeading, true)
        binding.etLogin.doAfterTextChanged {
            viewModel.onAction(LoginAction.UsernameChanged(it.toString()))
        }
        binding.etPassword.doAfterTextChanged {
            viewModel.onAction(LoginAction.PasswordChanged(it.toString()))
        }
        binding.btnLogin.setOnClickListener { viewModel.onAction(LoginAction.Submit) }
        binding.btnClear.setOnClickListener { viewModel.onAction(LoginAction.RequestClear) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch { viewModel.uiState.collect { renderState(binding, it) } }
                launch {
                    viewModel.effects.collect { effect ->
                        snackbar?.dismiss()
                        snackbar = Snackbar.make(view, effect.messageResource, Snackbar.LENGTH_SHORT)
                            .also { it.show() }
                    }
                }
            }
        }
    }

    private fun renderState(binding: FragmentLoginBinding, state: LoginUiState) {
        if (binding.etLogin.text.toString() != state.username) binding.etLogin.setText(state.username)
        if (binding.etPassword.text.toString() != state.password) binding.etPassword.setText(state.password)
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

    override fun onDestroyView() {
        snackbar?.dismiss()
        snackbar = null
        super.onDestroyView()
    }

    companion object {
        private const val DIALOG_TAG = "login_dialog"
    }
}
