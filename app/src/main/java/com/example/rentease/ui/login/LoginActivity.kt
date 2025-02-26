package com.example.rentease.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rentease.auth.AuthManager
import com.example.rentease.auth.UserType
import com.example.rentease.databinding.ActivityLoginBinding
import com.example.rentease.di.RepositoryProvider
import com.example.rentease.ui.properties.PropertyListActivity
import com.example.rentease.ui.register.RegisterActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels { 
        LoginViewModel.Factory(RepositoryProvider.provideAuthRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLoginButton()
        setupRegisterButton()
        observeViewModel()

        // Check if already logged in
        if (AuthManager.getInstance(this).isLoggedIn) {
            navigateToPropertyList()
        }
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()
            viewModel.login(username, password, UserType.LANDLORD)
        }
    }

    private fun setupRegisterButton() {
        binding.registerLink.setOnClickListener {
            startActivity(RegisterActivity.createIntent(this))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        LoginUiState.Initial -> Unit
                        LoginUiState.Loading -> showLoading()
                        is LoginUiState.Success -> navigateToPropertyList()
                        is LoginUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.loginButton.isEnabled = false
        binding.registerLink.isEnabled = false
        // Show loading indicator if needed
    }

    private fun navigateToPropertyList() {
        startActivity(PropertyListActivity.createIntent(this))
        finishAffinity()
    }

    private fun showError(message: String) {
        binding.loginButton.isEnabled = true
        binding.registerLink.isEnabled = true
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}
