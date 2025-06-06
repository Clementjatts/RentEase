package com.example.rentease.ui.propertyform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.rentease.databinding.FragmentPropertyFormBinding
import com.example.rentease.ui.utils.WindowInsetsHelper
import kotlinx.coroutines.launch

/**
 * PropertyFormFragment handles property creation and editing.
 * Simplified implementation without BaseFragment complexity.
 */
class PropertyFormFragment : Fragment() {

    private var _binding: FragmentPropertyFormBinding? = null
    private val binding get() = _binding!!

    private val args: PropertyFormFragmentArgs by navArgs()
    private val propertyId: Int by lazy { args.propertyId }

    private val viewModel: PropertyFormViewModel by viewModels {
        PropertyFormViewModel.Factory(requireActivity().application, propertyId)
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.addImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropertyFormBinding.inflate(inflater, container, false)
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
        setupImagesRecyclerView()
        setupAddImageButton()
        setupFurnitureTypeDropdown()
        setupSaveButton()
    }

    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Set title based on whether we're creating or editing
        val titleRes = if (propertyId == -1) {
            R.string.title_create_property
        } else {
            R.string.title_edit_property
        }
        binding.toolbar.title = getString(titleRes)
    }

    private fun setupImagesRecyclerView() {
        // No longer needed for single image, but keeping for now
        // Will be replaced with simple ImageView
    }

    private fun setupAddImageButton() {
        binding.addImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }
    }

    private fun setupFurnitureTypeDropdown() {
        val furnitureTypes = arrayOf(
            getString(R.string.furniture_furnished),
            getString(R.string.furniture_semi_furnished),
            getString(R.string.furniture_unfurnished)
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, furnitureTypes)
        binding.furnitureTypeInput.setAdapter(adapter)

        // Set default selection to "Unfurnished"
        binding.furnitureTypeInput.setText(getString(R.string.furniture_unfurnished), false)
    }

    private fun updateImageDisplay(image: PropertyImageItem?) {
        if (image != null) {
            // Show the image
            binding.propertyImageView.visibility = View.VISIBLE
            binding.removeImageButton.visibility = View.VISIBLE
            binding.addImageButton.text = getString(R.string.replace_image)

            // Load image using Glide
            Glide.with(this)
                .load(image.uri)
                .placeholder(R.drawable.placeholder_property)
                .error(R.drawable.placeholder_property)
                .centerCrop()
                .into(binding.propertyImageView)

            // Setup remove button
            binding.removeImageButton.setOnClickListener {
                viewModel.removeImage()
            }
        } else {
            // Hide the image
            binding.propertyImageView.visibility = View.GONE
            binding.removeImageButton.visibility = View.GONE
            binding.addImageButton.text = getString(R.string.add_image)
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val address = binding.addressInput.text.toString()
            val priceText = binding.priceInput.text.toString()
            val bedroomCount = binding.bedroomCountInput.text.toString()
            val bathroomCount = binding.bathroomCountInput.text.toString()
            val furnitureType = binding.furnitureTypeInput.text.toString()

            if (title.isBlank() || description.isBlank() || address.isBlank() || priceText.isBlank()) {
                Toast.makeText(requireContext(), R.string.error_all_fields_required, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (priceText.toDoubleOrNull() == null || priceText.toDoubleOrNull()!! <= 0) {
                Toast.makeText(requireContext(), R.string.error_invalid_price, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            viewModel.saveProperty(
                title = title,
                description = description,
                address = address,
                price = priceText,
                bedroomCount = bedroomCount,
                bathroomCount = bathroomCount,
                furnitureType = furnitureType
            )
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is PropertyFormUiState.Initial -> showInitialState()
                            is PropertyFormUiState.Loading -> showLoadingState()
                            is PropertyFormUiState.Success -> handleSuccess()
                            is PropertyFormUiState.Error -> showError(state.message)
                            is PropertyFormUiState.PropertyData -> showPropertyData(state)
                        }
                    }
                }

                launch {
                    viewModel.image.collect { image ->
                        updateImageDisplay(image)
                    }
                }
            }
        }

        // Load property data if editing
        if (propertyId != -1) {
            viewModel.loadPropertyDetails()
        }
    }

    private fun showInitialState() {
        binding.loadingIndicator.visibility = View.GONE
        binding.saveButton.isEnabled = true
        binding.addImageButton.isEnabled = true
    }

    private fun showLoadingState() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.saveButton.isEnabled = false
        binding.addImageButton.isEnabled = false
    }

    private fun handleSuccess() {
        binding.loadingIndicator.visibility = View.GONE

        // Show success message
        Toast.makeText(
            requireContext(),
            if (propertyId == -1) R.string.message_property_created else R.string.message_property_updated,
            Toast.LENGTH_LONG
        ).show()

        // Navigate back
        findNavController().popBackStack()
    }

    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.saveButton.isEnabled = true
        binding.addImageButton.isEnabled = true

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showPropertyData(state: PropertyFormUiState.PropertyData) {
        binding.titleInput.setText(state.title)
        binding.descriptionInput.setText(state.description)
        binding.addressInput.setText(state.address)
        binding.priceInput.setText(state.price)
        binding.bedroomCountInput.setText(state.bedroomCount)
        binding.bathroomCountInput.setText(state.bathroomCount)
        binding.furnitureTypeInput.setText(state.furnitureType, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(propertyId: Int = -1) = PropertyFormFragment().apply {
            arguments = Bundle().apply {
                putInt("propertyId", propertyId)
            }
        }
    }
}
