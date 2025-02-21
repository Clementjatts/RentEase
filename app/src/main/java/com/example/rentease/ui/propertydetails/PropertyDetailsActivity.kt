package com.example.rentease.ui.propertydetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rentease.R
import com.example.rentease.data.model.Property
import com.example.rentease.databinding.ActivityPropertyDetailsBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PropertyDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPropertyDetailsBinding
    private lateinit var imageAdapter: PropertyImageAdapter

    private val viewModel: PropertyDetailsViewModel by viewModels {
        PropertyDetailsViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupImageGallery()
        setupRefreshLayout()
        setupContactButton()
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
        imageAdapter = PropertyImageAdapter { image ->
            // TODO: Show full-screen image gallery
        }
        binding.imageViewPager.adapter = imageAdapter
        binding.imageIndicator.attachToViewPager(binding.imageViewPager)
    }

    private fun setupRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupContactButton() {
        binding.contactFab.setOnClickListener {
            val state = viewModel.uiState.value
            if (state is PropertyDetailsUiState.Success) {
                startActivity(ContactFormActivity.createIntent(this, state.property.id))
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PropertyDetailsUiState.Loading -> showLoading()
                        is PropertyDetailsUiState.Success -> showProperty(state.property)
                        is PropertyDetailsUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentGroup.visibility = View.GONE
    }

    private fun showProperty(property: Property) {
        binding.progressBar.visibility = View.GONE
        binding.contentGroup.visibility = View.VISIBLE
        binding.swipeRefreshLayout.isRefreshing = false

        with(binding) {
            propertyTitle.text = property.title
            propertyPrice.text = getString(R.string.property_price_format, property.price)
            propertyType.text = property.type
            propertyLocation.text = property.location
            propertyDescription.text = property.description
            landlordName.text = property.landlord.name
            landlordContact.text = property.landlord.email

            // Setup amenities
            amenitiesChipGroup.removeAllViews()
            property.amenities.forEach { amenity ->
                val chip = Chip(this@PropertyDetailsActivity).apply {
                    text = amenity
                    isClickable = false
                }
                amenitiesChipGroup.addView(chip)
            }

            // Update image gallery
            imageAdapter.submitList(property.images)
            imageIndicator.setViewPager(imageViewPager)
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.retry)) {
                viewModel.refresh()
            }
            .show()
    }

    companion object {
        fun createIntent(context: Context, propertyId: String): Intent {
            return Intent(context, PropertyDetailsActivity::class.java).apply {
                putExtra("property_id", propertyId)
            }
        }
    }
}
