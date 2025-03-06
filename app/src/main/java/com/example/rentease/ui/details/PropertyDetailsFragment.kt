package com.example.rentease.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.rentease.R
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import com.example.rentease.databinding.FragmentPropertyDetailsBinding
import com.example.rentease.ui.base.BaseFragment
import com.example.rentease.ui.navigation.NavigationHelper
import com.example.rentease.ui.propertydetails.PropertyImageAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class PropertyDetailsFragment : BaseFragment<FragmentPropertyDetailsBinding>() {
    
    private val propertyId: Int by lazy {
        arguments?.getInt(ARG_PROPERTY_ID, -1) ?: -1
    }
    
    private val viewModel: PropertyDetailsViewModel by viewModels {
        PropertyDetailsViewModel.Factory(
            propertyId = propertyId,
            repository = PropertyRepository(ApiClient.api, requireContext())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupBackNavigation()
    }
    
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_property_details, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                        // TODO: Navigate to edit screen
                        true
                    }
                    R.id.action_delete -> {
                        confirmDelete()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPropertyDetailsBinding {
        return FragmentPropertyDetailsBinding.inflate(inflater, container, false)
    }

    private lateinit var imageAdapter: PropertyImageAdapter
    
    override fun setupUI() {
        setupToolbar()
        setupImageGallery()
        setupContactButton()
    }
    
    private fun setupImageGallery() {
        imageAdapter = PropertyImageAdapter { imageUrl ->
            // Show full-screen image gallery when an image is clicked
            val property = (viewModel.uiState.value as? PropertyDetailsUiState.Success)?.property
            property?.let {
                val imageUrls = it.images ?: emptyList()
                val position = imageUrls.indexOf(imageUrl).coerceAtLeast(0)
                
                // Navigate to FullScreenImageFragment using NavigationHelper
                NavigationHelper.navigateToFullScreenImage(
                    findNavController(),
                    imageUrls.toTypedArray(),
                    position
                )
            }
        }
        
        binding.imageViewPager.adapter = imageAdapter
        
        // Connect the TabLayout dots indicator with the ViewPager
        TabLayoutMediator(binding.imageIndicator, binding.imageViewPager) { _, _ -> }.attach()
    }
    
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupContactButton() {
        binding.contactFab.setOnClickListener {
            val property = (viewModel.uiState.value as? PropertyDetailsUiState.Success)?.property
            property?.let {
                // Navigate to RequestFormFragment using NavigationHelper
                com.example.rentease.ui.navigation.NavigationHelper.navigateToRequestForm(
                    findNavController(),
                    propertyId = it.id,
                    landlordId = it.landlordId
                )
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
            loadingIndicator.visibility = View.VISIBLE
        }
    }

    private fun showProperty(property: Property) {
        binding.apply {
            propertyTitle.visibility = View.VISIBLE
            propertyDescription.visibility = View.VISIBLE
            landlordCard.visibility = View.VISIBLE
            contactFab.visibility = View.VISIBLE
            loadingIndicator.visibility = View.GONE

            propertyTitle.text = property.title
            propertyDescription.text = property.description
            landlordName.text = property.landlordName
            
            // Set property images if available
            property.images?.let { images ->
                if (images.isNotEmpty()) {
                    imageAdapter.submitList(images)
                    imageViewPager.visibility = View.VISIBLE
                    imageIndicator.visibility = View.VISIBLE
                } else {
                    imageViewPager.visibility = View.GONE
                    imageIndicator.visibility = View.GONE
                }
            } ?: run {
                imageViewPager.visibility = View.GONE
                imageIndicator.visibility = View.GONE
            }
            
            // Set landlord contact info if available
            property.landlordContact?.let { contact ->
                val contactParts = contact.split(",")
                if (contactParts.isNotEmpty()) {
                    landlordEmail.text = contactParts[0].trim()
                    landlordEmail.visibility = View.VISIBLE
                } else {
                    landlordEmail.visibility = View.GONE
                }
                
                if (contactParts.size > 1) {
                    landlordPhone.text = contactParts[1].trim()
                    landlordPhone.visibility = View.VISIBLE
                } else {
                    landlordPhone.visibility = View.GONE
                }
            } ?: run {
                landlordEmail.visibility = View.GONE
                landlordPhone.visibility = View.GONE
            }
            
            // Set property price
            propertyPrice.text = getString(R.string.price_format, property.price.toString())
            
            // Set property type
            propertyType.text = property.type ?: "Property"
            
            // Set property location
            propertyLocation.text = property.address
        }
    }

    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    // Deprecated menu methods removed and replaced with MenuProvider in setupMenu()

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Property")
            .setMessage("Are you sure you want to delete this property?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteProperty {
                    // Navigate back
                    findNavController().navigateUp()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        private const val ARG_PROPERTY_ID = "arg_property_id"

        fun newInstance(propertyId: Int): PropertyDetailsFragment {
            return PropertyDetailsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PROPERTY_ID, propertyId)
                }
            }
        }
    }
}
