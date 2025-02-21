package com.example.rentease.ui.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rentease.databinding.ActivityRegisterBinding
import com.example.rentease.ui.login.LoginActivity
import com.example.rentease.ui.properties.PropertyListActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRegisterButton()
        setupLoginButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { 
            finish() 
        }
    }

    private fun setupRegisterButton() {
        binding.registerButton.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()

            if (password != confirmPassword) {
                showError(getString(R.string.error_passwords_dont_match))
                return@setOnClickListener
            }

            viewModel.register(name, email, password)
        }
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            startActivity(LoginActivity.createIntent(this))
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        RegisterState.Initial -> Unit
                        RegisterState.Loading -> showLoading()
                        RegisterState.Success -> navigateToPropertyList()
                        is RegisterState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.registerButton.isEnabled = false
        binding.loginButton.isEnabled = false
        // Show loading indicator if needed
    }

    private fun navigateToPropertyList() {
        startActivity(PropertyListActivity.createIntent(this))
        finishAffinity()
    }

    private fun showError(message: String) {
        binding.registerButton.isEnabled = true
        binding.loginButton.isEnabled = true
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, RegisterActivity::class.java)
    }
}
