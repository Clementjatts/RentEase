package com.example.rentease.ui.propertyform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.local.RentEaseDatabase
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import com.example.rentease.databinding.ActivityPropertyFormBinding
import kotlinx.coroutines.launch

class PropertyFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPropertyFormBinding
    private lateinit var imagesAdapter: PropertyImagesAdapter

    private val viewModel: PropertyFormViewModel by viewModels {
        val database = RentEaseDatabase.getDatabase(applicationContext)
        PropertyFormViewModel.Factory(
            propertyId = intent.getIntExtra(EXTRA_PROPERTY_ID, -1).takeIf { it != -1 },
            repository = PropertyRepository(ApiClient.api, database.propertyDao()),
            authManager = AuthManager.getInstance(applicationContext),
            savedStateHandle = defaultViewModelCreationExtras.createSavedStateHandle()
        )
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.addImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupImagesRecyclerView()
        setupButtons()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (intent.hasExtra(EXTRA_PROPERTY_ID)) {
                "Edit Property"
            } else {
                "Add Property"
            }
        }
    }

    private fun setupImagesRecyclerView() {
        imagesAdapter = PropertyImagesAdapter(
            onDeleteClick = { uri -> viewModel.removeImage(uri) }
        )
        binding.imagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PropertyFormActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = imagesAdapter
        }
    }

    private fun setupButtons() {
        binding.addImagesButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.saveButton.setOnClickListener {
            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val address = binding.addressInput.text.toString()
            val price = binding.priceInput.text.toString()

            viewModel.saveProperty(title, description, address, price)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            PropertyFormUiState.Initial -> {
                                // Initial state, no action needed
                            }
                            PropertyFormUiState.Loading -> showLoading()
                            is PropertyFormUiState.Success -> showProperty(state.property)
                            PropertyFormUiState.Saved -> {
                                hideLoading()
                                finish()
                            }
                            is PropertyFormUiState.Error -> showError(state.message)
                        }
                    }
                }

                launch {
                    viewModel.selectedImages.collect { images ->
                        imagesAdapter.submitList(images)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.saveButton.isEnabled = false
        // TODO: Add progress indicator
    }

    private fun hideLoading() {
        binding.saveButton.isEnabled = true
    }

    private fun showProperty(property: Property) {
        hideLoading()
        binding.apply {
            titleInput.setText(property.title)
            descriptionInput.setText(property.description)
            // TODO: Set other fields when added to the Property model
        }
    }

    private fun showError(message: String) {
        hideLoading()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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
        private const val EXTRA_PROPERTY_ID = "extra_property_id"

        fun createIntent(context: Context, propertyId: Int? = null): Intent {
            return Intent(context, PropertyFormActivity::class.java).apply {
                propertyId?.let { putExtra(EXTRA_PROPERTY_ID, it) }
            }
        }
    }
}
