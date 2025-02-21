package com.example.rentease.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rentease.auth.AuthManager
import com.example.rentease.auth.UserType
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.repository.AuthRepository
import com.example.rentease.databinding.ActivityLoginBinding
import com.example.rentease.ui.properties.PropertyListActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        val authManager = AuthManager.getInstance(applicationContext)
        val repository = AuthRepository(ApiClient.api, authManager)
        LoginViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTabLayout()
        setupLoginButton()
        observeViewModel()

        // Check if already logged in
        if (AuthManager.getInstance(this).isLoggedIn) {
            navigateToMain()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Clear any error messages when switching tabs
                binding.errorText.visibility = View.GONE
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val userType = if (binding.tabLayout.selectedTabPosition == 0) {
                UserType.ADMIN
            } else {
                UserType.LANDLORD
            }

            viewModel.login(username, password, userType)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        LoginUiState.Initial -> {
                            // Initial state, no action needed
                        }
                        LoginUiState.Loading -> showLoading()
                        is LoginUiState.Success -> {
                            hideLoading()
                            showWelcomeMessage(state.user.fullName ?: state.user.username)
                            navigateToMain()
                        }
                        is LoginUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.loginButton.isEnabled = false
        binding.errorText.visibility = View.GONE
        // TODO: Add progress indicator
    }

    private fun hideLoading() {
        binding.loginButton.isEnabled = true
    }

    private fun showError(message: String) {
        hideLoading()
        binding.errorText.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun showWelcomeMessage(name: String) {
        Snackbar.make(
            binding.root,
            "Welcome back, $name!",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun navigateToMain() {
        startActivity(PropertyListActivity.createIntent(this))
        finish()
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}
