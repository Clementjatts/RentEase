package com.example.rentease.ui.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rentease.R
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.local.RentEaseDatabase
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import com.example.rentease.databinding.ActivityPropertyDetailsBinding
import com.example.rentease.ui.contact.ContactFormActivity
import kotlinx.coroutines.launch

class PropertyDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPropertyDetailsBinding

    private val viewModel: PropertyDetailsViewModel by viewModels {
        val database = RentEaseDatabase.getDatabase(applicationContext)
        PropertyDetailsViewModel.Factory(
            propertyId = intent.getIntExtra(EXTRA_PROPERTY_ID, -1),
            repository = PropertyRepository(ApiClient.api, database.propertyDao()),
            savedStateHandle = defaultViewModelCreationExtras.createSavedStateHandle()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupContactButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupContactButton() {
        binding.contactFab.setOnClickListener {
            val property = (viewModel.uiState.value as? PropertyDetailsUiState.Success)?.property
            property?.let {
                startActivity(ContactFormActivity.createIntent(this, it.id))
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
        binding.apply {
            propertyTitle.visibility = View.GONE
            propertyDescription.visibility = View.GONE
            landlordCard.visibility = View.GONE
            contactFab.visibility = View.GONE
            // TODO: Add progress indicator
        }
    }

    private fun showProperty(property: Property) {
        binding.apply {
            propertyTitle.visibility = View.VISIBLE
            propertyDescription.visibility = View.VISIBLE
            landlordCard.visibility = View.VISIBLE
            contactFab.visibility = View.VISIBLE

            propertyTitle.text = property.title
            propertyDescription.text = property.description
            landlordName.text = property.landlordName
            landlordContact.text = property.landlordContact
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_property_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                // TODO: Navigate to edit screen
                true
            }
            R.id.action_delete -> {
                confirmDelete()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Property")
            .setMessage("Are you sure you want to delete this property?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteProperty {
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
