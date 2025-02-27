package com.example.rentease.ui.propertyform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentease.R
import com.example.rentease.databinding.FragmentPropertyFormBinding
import com.example.rentease.ui.base.BaseFragment
import kotlinx.coroutines.launch

/**
 * PropertyFormFragment handles property creation and editing.
 * It replaces the PropertyFormActivity in the fragment-based architecture.
 */
class PropertyFormFragment : BaseFragment<FragmentPropertyFormBinding>() {
    
    private val args: PropertyFormFragmentArgs by navArgs()
    private val propertyId: Int by lazy { args.propertyId }
    
    private val viewModel: PropertyFormViewModel by viewModels {
        PropertyFormViewModel.Factory(requireActivity().application, propertyId)
    }
    
    private lateinit var imagesAdapter: PropertyImagesAdapter
    
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.addImage(uri)
            }
        }
    }
    
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPropertyFormBinding {
        return FragmentPropertyFormBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        setupToolbar()
        setupImagesRecyclerView()
        setupAddImageButton()
        setupSaveButton()
    }
    
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
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
        imagesAdapter = PropertyImagesAdapter(
            onDeleteClick = { position ->
                viewModel.removeImage(position)
            }
        )
        
        binding.imagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imagesAdapter
        }
    }
    
    private fun setupAddImageButton() {
        binding.addImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }
    }
    
    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val address = binding.addressInput.text.toString()
            val priceText = binding.priceInput.text.toString()
            
            if (title.isBlank() || description.isBlank() || address.isBlank() || priceText.isBlank()) {
                Toast.makeText(requireContext(), R.string.error_all_fields_required, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            val price = priceText.toDoubleOrNull()
            if (price == null || price <= 0) {
                Toast.makeText(requireContext(), R.string.error_invalid_price, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            viewModel.saveProperty(
                title = title,
                description = description,
                address = address,
                price = price
            )
        }
    }
    
    override fun setupObservers() {
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
                    viewModel.images.collect { images ->
                        imagesAdapter.submitList(images)
                    }
                }
            }
        }
        
        // Load property data if editing
        if (propertyId != -1) {
            viewModel.loadProperty()
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
        binding.priceInput.setText(state.price.toString())
    }
    
    companion object {
        fun newInstance(propertyId: Int = -1) = PropertyFormFragment().apply {
            arguments = Bundle().apply {
                putInt("propertyId", propertyId)
            }
        }
    }
}
