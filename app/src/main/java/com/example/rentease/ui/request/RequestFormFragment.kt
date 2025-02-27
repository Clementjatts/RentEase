package com.example.rentease.ui.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.rentease.databinding.FragmentRequestFormBinding
import com.example.rentease.notification.NotificationService
import com.example.rentease.ui.base.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * RequestFormFragment handles contact requests for properties.
 * It replaces the RequestFormActivity in the fragment-based architecture.
 */
class RequestFormFragment : BaseFragment<FragmentRequestFormBinding>() {
    
    private val args: RequestFormFragmentArgs by navArgs()
    
    private val viewModel: RequestFormViewModel by viewModels {
        RequestFormViewModel.Factory(requireActivity().application, args.propertyId, args.landlordId)
    }
    
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRequestFormBinding {
        return FragmentRequestFormBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        setupToolbar()
        setupSubmitButton()
        
        // Load property details
        viewModel.loadPropertyDetails()
    }
    
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupSubmitButton() {
        binding.submitButton.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val phone = binding.phoneInput.text.toString()
            val message = binding.messageInput.text.toString()
            
            if (validateInputs(name, email, message)) {
                viewModel.submitRequest(name, email, phone, message)
            }
        }
    }
    
    private fun validateInputs(name: String, email: String, message: String): Boolean {
        var isValid = true
        
        if (name.isBlank()) {
            binding.nameInputLayout.error = "Name is required"
            isValid = false
        } else {
            binding.nameInputLayout.error = null
        }
        
        if (email.isBlank()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Invalid email format"
            isValid = false
        } else {
            binding.emailInputLayout.error = null
        }
        
        if (message.isBlank()) {
            binding.messageInputLayout.error = "Message is required"
            isValid = false
        } else {
            binding.messageInputLayout.error = null
        }
        
        return isValid
    }
    
    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is RequestFormUiState.Loading -> showLoading()
                            is RequestFormUiState.PropertyLoaded -> showPropertyDetails(state)
                            is RequestFormUiState.Success -> handleSuccess()
                            is RequestFormUiState.Error -> showError(state.message)
                        }
                    }
                }
            }
        }
    }
    
    private fun showLoading() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.submitButton.isEnabled = false
    }
    
    private fun showPropertyDetails(state: RequestFormUiState.PropertyLoaded) {
        binding.loadingIndicator.visibility = View.GONE
        binding.submitButton.isEnabled = true
        
        binding.propertyTitle.text = state.property.title
        binding.propertyAddress.text = state.property.address
    }
    
    private fun handleSuccess() {
        binding.loadingIndicator.visibility = View.GONE
        
        // Show notification
        val notificationService = NotificationService.getInstance(requireContext())
        notificationService.showRequestSubmittedNotification()
        
        // Show success message
        Snackbar.make(binding.root, "Request submitted successfully", Snackbar.LENGTH_LONG).show()
        
        // Navigate back
        findNavController().navigateUp()
    }
    
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.submitButton.isEnabled = true
        
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    companion object {
        fun newInstance() = RequestFormFragment()
    }
}
