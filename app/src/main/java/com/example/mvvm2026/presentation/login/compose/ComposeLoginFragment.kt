package com.example.mvvm2026.presentation.login.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mvvm2026.presentation.login.MainViewModel
import com.example.mvvm2026.presentation.theme.Mvvm2026Theme

/**
 * Hosts the Compose renderer with the activity-scoped ViewModel.
 * The composition ends with the fragment view so replacing this renderer also cancels its effects.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
class ComposeLoginFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mvvm2026Theme {
                    LoginRoute(viewModel)
                }
            }
        }
}
