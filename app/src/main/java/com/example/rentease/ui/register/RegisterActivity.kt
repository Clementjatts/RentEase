package com.example.rentease.ui.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rentease.R
import com.example.rentease.auth.UserType
import com.example.rentease.databinding.ActivityRegisterBinding
import com.example.rentease.ui.login.LoginActivity
import com.example.rentease.ui.properties.PropertyListActivity
import com.example.rentease.ui.register.RegisterUiState
import com.example.rentease.ui.register.RegisterViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()
    private var selectedUserType = UserType.LANDLORD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUserTypeSelection()
        setupRegisterButton()
        setupLoginPrompt()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { 
            finish() 
        }
    }

    private fun setupUserTypeSelection() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                selectedUserType = when (tab?.position) {
                    0 -> UserType.ADMIN
                    else -> UserType.LANDLORD
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun setupRegisterButton() {
        binding.registerButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val fullName = binding.fullNameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val phone = binding.phoneInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()

            if (password != confirmPassword) {
                showError(getString(R.string.error_passwords_dont_match))
                return@setOnClickListener
            }

            viewModel.register(
                username = username,
                password = password,
                confirmPassword = confirmPassword,
                email = email,
                fullName = fullName,
                phone = phone,
                userType = selectedUserType
            )
        }
    }

    private fun setupLoginPrompt() {
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
                        RegisterUiState.Initial -> Unit
                        RegisterUiState.Loading -> showLoading()
                        is RegisterUiState.Success -> navigateToPropertyList()
                        is RegisterUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.registerButton.isEnabled = false
        binding.loginPrompt.isEnabled = false
        // Show loading indicator if needed
    }

    private fun navigateToPropertyList() {
        startActivity(PropertyListActivity.createIntent(this))
        finishAffinity()
    }

    private fun showError(message: String) {
        binding.registerButton.isEnabled = true
        binding.loginPrompt.isEnabled = true
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, RegisterActivity::class.java)
    }
}
