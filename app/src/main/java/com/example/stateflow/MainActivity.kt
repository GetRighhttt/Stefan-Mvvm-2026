/**
 * Hosts the XML and Compose examples under one activity so both fragments can use the same ViewModel.
 *
 * `onCreate()` applies window and keyboard insets to the shared parent. It uses the fragment restored by
 * FragmentManager before setting the switch value, then attaches the switch listener. That order keeps
 * activity recreation from replacing an already restored example.
 *
 * `showExample()` replaces the fragment without adding a back-stack entry. The old fragment's view and
 * collectors end, while the activity's ViewModel keeps the form and any pending login. The switch
 * changes the renderer; it does not create a second login session.
 */
package com.example.stateflow

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stateflow.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
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
        binding.useCompose.setOnCheckedChangeListener { _, checked -> showExample(checked) }
    }

    private fun showExample(useCompose: Boolean) {
        val fragment = if (useCompose) ComposeLoginFragment() else XmlLoginFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.login_container, fragment)
            .commitNow()
    }
}
