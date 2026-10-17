package com.example.mvvm2026.presentation

import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mvvm2026.R
import com.example.mvvm2026.databinding.ActivityMainBinding
import com.example.mvvm2026.di.LoginViewModelFactory
import com.example.mvvm2026.presentation.login.MainViewModel
import com.example.mvvm2026.presentation.login.compose.ComposeLoginFragment
import com.example.mvvm2026.presentation.login.xml.XmlLoginFragment
import kotlin.math.ceil
import kotlinx.coroutines.launch

/**
 * Hosts the renderer switch and supplies the ViewModel factory used by both fragments.
 * The activity owns the ViewModel lifetime and applies window insets once to the shared parent.
 * The window background follows the active renderer, including its header and inset areas.
 * Its resumed lifecycle runs one account-notice countdown across renderer changes. The duration
 * respects Android's accessibility timeout; leaving the foreground pauses the remaining time.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = LoginViewModelFactory.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
            )
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
        ViewCompat.requestApplyInsets(binding.root)

        if (supportFragmentManager.findFragmentById(R.id.login_container) == null) {
            showExample(useCompose = false)
        }
        binding.useCompose.isChecked =
            supportFragmentManager.findFragmentById(R.id.login_container) is ComposeLoginFragment
        updateRendererBackground(binding.useCompose.isChecked)
        binding.useCompose.setOnCheckedChangeListener { _, checked -> showExample(checked) }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.runAccountNoticeCountdown(accountNoticeDurationSeconds())
            }
        }
    }

    private fun accountNoticeDurationSeconds(): Int {
        val timeoutMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getSystemService(AccessibilityManager::class.java).getRecommendedTimeoutMillis(
                8_000,
                AccessibilityManager.FLAG_CONTENT_TEXT or AccessibilityManager.FLAG_CONTENT_CONTROLS or
                    AccessibilityManager.FLAG_CONTENT_ICONS
            )
        } else 8_000
        return ceil(timeoutMillis / 1_000.0).toInt()
    }

    private fun showExample(useCompose: Boolean) {
        updateRendererBackground(useCompose)
        val fragment = if (useCompose) ComposeLoginFragment() else XmlLoginFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.login_container, fragment)
            .commitNow()
    }

    private fun updateRendererBackground(useCompose: Boolean) {
        window.setBackgroundDrawableResource(
            if (useCompose) R.color.compose_background else R.color.xml_background
        )
    }
}
