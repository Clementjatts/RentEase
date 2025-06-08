package com.example.rentease.ui.propertydetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.rentease.R
import com.example.rentease.data.model.Property
import com.example.rentease.databinding.FragmentPropertyDetailsBinding
import com.example.rentease.di.RepositoryProvider
import com.example.rentease.ui.helpers.WindowInsetsHelper
import kotlinx.coroutines.launch

/**
 * PropertyDetailsFragment displays detailed information about a property.
 */
class PropertyDetailsFragment : Fragment() {

    private var _binding: FragmentPropertyDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: PropertyDetailsFragmentArgs by navArgs()
    private val viewModel: PropertyDetailsViewModel by viewModels {
        PropertyDetailsViewModel.Factory(
            args.propertyId,
            RepositoryProvider.providePropertyRepository(requireActivity().application)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropertyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for proper edge-to-edge display
        WindowInsetsHelper.applyWindowInsets(binding.root, binding.appBarLayout)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        setupToolbar()
        setupContactButton()
    }

    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupContactButton() {
        binding.contactFab.setOnClickListener {
            // Navigate to RequestFormFragment with property ID
            val action = PropertyDetailsFragmentDirections
                .actionPropertyDetailsFragmentToRequestFormFragment(args.propertyId)
            findNavController().navigate(action)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PropertyDetailsUiState.Loading -> showLoading()
                        is PropertyDetailsUiState.Success -> showPropertyDetails(state.property)
                        is PropertyDetailsUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.mainLoadingIndicator.visibility = View.VISIBLE
        binding.loadingIndicator.visibility = View.GONE
    }

    private fun showPropertyDetails(property: Property) {
        binding.mainLoadingIndicator.visibility = View.GONE
        binding.loadingIndicator.visibility = View.GONE

        binding.apply {
            // Set property details
            propertyTitle.text = property.title
            propertyPrice.text = getString(R.string.price_format, String.format("%.0f", property.price))
            // Set location with address first, then bed/bath info (matching PropertyListAdapter format)
            val locationText = buildString {
                // Add address first
                if (property.address.isNotEmpty()) {
                    append(property.address)
                } else {
                    append(getString(R.string.no_address_provided))
                }
                // Add bed/bath specifications after address
                append(" • ${property.bedroomCount} bed")
                if (property.bathroomCount > 0) {
                    append(" • ${property.bathroomCount} bath")
                }
            }
            propertyLocation.text = locationText

            // Set property description (furniture type already shown in chip, so just description)
            propertyDescription.text = if (!property.description.isNullOrEmpty()) {
                property.description
            } else {
                getString(R.string.no_description_provided)
            }

            // Set property type chip to show furniture type (since it's more relevant for users)
            if (!property.furnitureType.isNullOrEmpty() && property.furnitureType != "unfurnished") {
                propertyType.text = property.furnitureType.replaceFirstChar { it.uppercase() }
                propertyType.visibility = View.VISIBLE
            } else if (!property.type.isNullOrEmpty()) {
                propertyType.text = property.type.replaceFirstChar { it.uppercase() }
                propertyType.visibility = View.VISIBLE
            } else {
                propertyType.visibility = View.GONE
            }

            // Set landlord details
            landlordName.text = property.landlordName ?: getString(R.string.landlord_info_unavailable)

            // Parse landlord contact (format: "email,phone")
            val contactInfo = property.landlordContact
            if (!contactInfo.isNullOrEmpty()) {
                val contactParts = contactInfo.split(",")
                if (contactParts.size >= 2) {
                    // Extract email and phone from the contact string
                    val email = contactParts[0].trim()
                    val phone = contactParts[1].trim()
                    landlordEmail.text = email
                    landlordPhone.text = phone
                    landlordPhone.visibility = View.VISIBLE
                } else if (contactParts.size == 1) {
                    // Only one contact method available
                    val contact = contactParts[0].trim()
                    if (contact.contains("@")) {
                        // It's an email
                        landlordEmail.text = contact
                        landlordPhone.visibility = View.GONE
                    } else {
                        // It's a phone number
                        landlordEmail.text = getString(R.string.email_not_provided)
                        landlordPhone.text = contact
                        landlordPhone.visibility = View.VISIBLE
                    }
                }
            } else {
                // No contact information available
                landlordEmail.text = getString(R.string.email_not_provided)
                landlordPhone.visibility = View.GONE
            }

            // Load property image
            if (!property.imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(property.imageUrl)
                    .placeholder(R.drawable.placeholder_property)
                    .error(R.drawable.placeholder_property)
                    .centerCrop()
                    .into(propertyImageView)
            } else {
                propertyImageView.setImageResource(R.drawable.placeholder_property)
            }
        }
    }

    private fun showError(message: String) {
        binding.mainLoadingIndicator.visibility = View.GONE
        binding.loadingIndicator.visibility = View.GONE
        // TODO: Show error message to user
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PropertyDetailsFragment()
    }
} 