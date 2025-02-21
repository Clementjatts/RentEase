package com.example.rentease.ui.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
import com.example.rentease.databinding.ActivityRegisterBinding
import com.example.rentease.ui.login.LoginActivity
import com.example.rentease.ui.properties.PropertyListActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: RegisterViewModel by viewModels {
        val authManager = AuthManager.getInstance(applicationContext)
        val repository = AuthRepository(ApiClient.api, authManager)
        RegisterViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTabLayout()
        setupButtons()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

    private fun setupButtons() {
        binding.registerButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()
            val email = binding.emailInput.text.toString()
            val fullName = binding.fullNameInput.text.toString()
            val phone = binding.phoneInput.text.toString()
            val userType = if (binding.tabLayout.selectedTabPosition == 0) {
                UserType.ADMIN
            } else {
                UserType.LANDLORD
            }

            viewModel.register(
                username = username,
                password = password,
                confirmPassword = confirmPassword,
                email = email,
                fullName = fullName,
                phone = phone,
                userType = userType
            )
        }

        binding.loginPrompt.setOnClickListener {
            startActivity(LoginActivity.createIntent(this))
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        RegisterUiState.Initial -> {
                            // Initial state, no action needed
                        }
                        RegisterUiState.Loading -> showLoading()
                        is RegisterUiState.Success -> {
                            hideLoading()
                            showWelcomeMessage(state.user.fullName ?: state.user.username)
                            navigateToMain()
                        }
                        is RegisterUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.registerButton.isEnabled = false
        binding.errorText.visibility = View.GONE
        // TODO: Add progress indicator
    }

    private fun hideLoading() {
        binding.registerButton.isEnabled = true
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
            "Welcome to RentEase, $name!",
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun navigateToMain() {
        startActivity(PropertyListActivity.createIntent(this))
        finishAffinity()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, RegisterActivity::class.java)
    }
}
