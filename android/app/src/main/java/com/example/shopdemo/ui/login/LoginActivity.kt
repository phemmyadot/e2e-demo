package com.example.shopdemo.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.shopdemo.data.ServiceLocator
import com.example.shopdemo.databinding.ActivityLoginBinding
import com.example.shopdemo.testing.LaunchConfig
import com.example.shopdemo.ui.main.MainActivity
import kotlinx.coroutines.launch

/**
 * Launcher + auth gate. Intentionally View-based (XML + ViewBinding) so the POC covers
 * Espresso as well as Compose UI Test — most large apps are a mix of both.
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        viewModelFactory { initializer { LoginViewModel(ServiceLocator.session) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LaunchConfig.apply(intent)
        if (ServiceLocator.session.isLoggedIn) {
            openStore()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginEmail.doAfterTextChanged { viewModel.onEmailChanged(it.toString()) }
        binding.loginPassword.doAfterTextChanged { viewModel.onPasswordChanged(it.toString()) }
        binding.loginSubmit.setOnClickListener { viewModel.submit() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::render)
            }
        }
    }

    private fun render(state: LoginUiState) {
        binding.loginSubmit.isEnabled = state.canSubmit
        binding.loginProgress.isVisible = state.isLoading
        binding.loginError.isVisible = state.error != null
        binding.loginError.text = state.error
        if (state.signedIn) openStore()
    }

    private fun openStore() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
