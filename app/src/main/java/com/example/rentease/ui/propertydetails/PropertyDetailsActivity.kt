package com.example.rentease.ui.propertydetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rentease.R
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import com.example.rentease.databinding.ActivityPropertyDetailsBinding
import com.example.rentease.ui.contact.ContactFormActivity
import com.example.rentease.ui.propertydetails.FullScreenImageActivity
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PropertyDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPropertyDetailsBinding
    private lateinit var imageAdapter: PropertyImageAdapter
    
    private val viewModel by lazy { 
        val repository = PropertyRepository(ApiClient.api, applicationContext)
        ViewModelProvider(this, PropertyDetailsViewModelFactory(repository, intent.getIntExtra(EXTRA_PROPERTY_ID, -1)))
            .get(PropertyDetailsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupImageGallery()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { 
            finish() 
        }
    }

    private fun setupImageGallery() {
        imageAdapter = PropertyImageAdapter { imageUrl ->
            // Show full-screen image gallery when an image is clicked
            val allImageUrls = viewModel.uiState.value.let { state ->
                if (state is PropertyDetailsUiState.Success) {
                    state.property.images ?: emptyList()
                } else {
                    emptyList()
                }
            }
            
            // Get the position of the clicked image
            val position = allImageUrls.indexOf(imageUrl).coerceAtLeast(0)
            
            // Launch the full-screen gallery
            val intent = FullScreenImageActivity.createIntent(
                this, 
                ArrayList(allImageUrls),
                position
            )
            startActivity(intent)
        }
        binding.imageViewPager.adapter = imageAdapter
        // Connect the TabLayout dots indicator with the ViewPager
        TabLayoutMediator(binding.imageIndicator, binding.imageViewPager) { _, _ -> }.attach()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PropertyDetailsUiState.Loading -> showLoadingState()
                        is PropertyDetailsUiState.Success -> showSuccessState(state.property)
                        is PropertyDetailsUiState.Error -> showErrorState(state.message)
                        is PropertyDetailsUiState.ContactReady -> handleContactReady(state)
                    }
                }
            }
        }
    }
    
    private fun showLoadingState() {
        binding.apply {
            // TODO: Add progress indicator to layout
        }
    }
    
    private fun showSuccessState(property: Property) {
        binding.apply {
            // Set property details
            propertyTitle.text = property.title
            propertyPrice.text = getString(R.string.price_format, 
                NumberFormat.getCurrencyInstance(Locale.US).format(property.price))
            propertyLocation.text = property.address
            propertyDescription.text = property.description
            
            // Set landlord info if available
            property.landlordName?.let { name ->
                landlordName.text = name
                landlordContact.text = property.landlordContact ?: ""
            }
            
            // Set images if available
            property.images?.let { images ->
                imageAdapter.submitList(images)
            }
            
            // Setup contact button
            contactFab.setOnClickListener {
                val propertyId = intent.getIntExtra(EXTRA_PROPERTY_ID, -1)
                if (propertyId != -1) {
                    startActivity(ContactFormActivity.createIntent(this@PropertyDetailsActivity, propertyId))
                }
            }
        }
    }
    
    private fun showErrorState(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun handleContactReady(state: PropertyDetailsUiState.ContactReady) {
        // Show contact form or dialog with landlord contact information
        val intent = ContactFormActivity.createIntent(
            this,
            state.property.id
        )
        startActivity(intent)
    }

    companion object {
        private const val EXTRA_PROPERTY_ID = "extra_property_id"

        fun createIntent(context: Context, propertyId: Int): Intent {
            return Intent(context, PropertyDetailsActivity::class.java).apply {
                putExtra(EXTRA_PROPERTY_ID, propertyId)
            }
        }
    }
}
