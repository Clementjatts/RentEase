package com.example.rentease.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rentease.R
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.model.User
import com.example.rentease.data.repository.AuthRepository
import com.example.rentease.databinding.ActivityProfileBinding
import com.example.rentease.ui.login.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    private val viewModel: ProfileViewModel by viewModels {
        val authManager = AuthManager.getInstance(applicationContext)
        val repository = AuthRepository(ApiClient.api, authManager)
        ProfileViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupButtons()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { 
            finish() 
        }
    }

    private fun setupButtons() {
        binding.changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        // Add logout functionality to the toolbar menu instead
        binding.saveButton.setOnClickListener {
            val fullName = binding.fullNameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val phone = binding.phoneInput.text.toString()

            viewModel.updateProfile(fullName, email, phone)
        }
    }

    private fun showChangePasswordDialog() {
        // TODO: Implement change password dialog
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.title_logout_confirmation))
            .setMessage(getString(R.string.message_logout_confirmation))
            .setPositiveButton(getString(R.string.button_logout)) { _, _ ->
                viewModel.logout()
                startActivity(LoginActivity.createIntent(this))
                finishAffinity()
            }
            .setNegativeButton(getString(R.string.button_cancel), null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        ProfileUiState.Loading -> showLoading()
                        is ProfileUiState.Success -> {
                            hideLoading()
                            showProfile(state.user)
                        }
                        is ProfileUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.saveButton.isEnabled = false
        binding.changePasswordButton.isEnabled = false
        // Show loading indicator if needed
    }

    private fun hideLoading() {
        binding.saveButton.isEnabled = true
        binding.changePasswordButton.isEnabled = true
    }

    private fun showProfile(user: User) {
        binding.apply {
            fullNameInput.setText(user.fullName)
            emailInput.setText(user.email)
            phoneInput.setText(user.phone)

            usernameText.text = "Username: ${user.username}"
            userTypeText.text = "Account Type: ${user.userType}"

            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            joinDateText.text = "Joined: ${dateFormat.format(user.createdAt)}"
        }
    }

    private fun showError(message: String) {
        hideLoading()
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }
}
