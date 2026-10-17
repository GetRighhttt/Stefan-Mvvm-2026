/**
 * Gives the Compose example the same fragment and ViewModel lifetime as the XML example.
 *
 * `onCreateView()` returns a ComposeView and chooses a Material color scheme from the system theme.
 * `DisposeOnViewTreeLifecycleDestroyed` ties the composition to this fragment's view. Replacing the
 * fragment disposes the composition and cancels the effect collector started by `LoginRoute()`.
 *
 * The activity owns the ViewModel and window insets. This fragment supplies the Compose host; the
 * state collection and UI implementation are in LoginScreen.kt.
 */
package com.example.stateflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class ComposeLoginFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                    LoginRoute(viewModel)
                }
            }
        }
}
