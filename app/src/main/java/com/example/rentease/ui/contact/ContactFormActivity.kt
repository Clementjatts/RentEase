package com.example.rentease.ui.contact

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.local.RentEaseDatabase
import com.example.rentease.data.repository.PropertyRepository
import com.example.rentease.databinding.ActivityContactFormBinding
import kotlinx.coroutines.launch

class ContactFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactFormBinding

    private val viewModel: ContactFormViewModel by viewModels {
        val database = RentEaseDatabase.getDatabase(applicationContext)
        ContactFormViewModel.Factory(
            propertyId = intent.getIntExtra(EXTRA_PROPERTY_ID, -1),
            repository = PropertyRepository(ApiClient.api, database.propertyDao()),
            savedStateHandle = defaultViewModelCreationExtras.createSavedStateHandle()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSubmitButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupSubmitButton() {
        binding.submitButton.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val message = binding.messageInput.text.toString()
            viewModel.submitRequest(name, email, message)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        ContactFormUiState.Initial -> {
                            // Initial state, no action needed
                        }
                        ContactFormUiState.Loading -> showLoading()
                        ContactFormUiState.Success -> {
                            showSuccess()
                            finish()
                        }
                        is ContactFormUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.submitButton.isEnabled = false
        // TODO: Add progress indicator
    }

    private fun showSuccess() {
        Toast.makeText(
            this,
            "Request sent successfully! The landlord will contact you soon.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showError(message: String) {
        binding.submitButton.isEnabled = true
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val EXTRA_PROPERTY_ID = "extra_property_id"

        fun createIntent(context: Context, propertyId: Int): Intent {
            return Intent(context, ContactFormActivity::class.java).apply {
                putExtra(EXTRA_PROPERTY_ID, propertyId)
            }
        }
    }
}
