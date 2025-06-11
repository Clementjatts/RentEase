package com.example.rentease.ui.request

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
import com.example.rentease.databinding.FragmentRequestFormBinding
import com.example.rentease.ui.helpers.WindowInsetsHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

// RequestFormFragment handles contact requests for properties
class RequestFormFragment : Fragment() {

    private var _binding: FragmentRequestFormBinding? = null
    private val binding get() = _binding!!

    private val args: RequestFormFragmentArgs by navArgs()

    private val viewModel: RequestFormViewModel by viewModels {
        RequestFormViewModel.Factory(requireActivity().application, args.propertyId)
    }

    // Creates the fragment's view hierarchy
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Initializes the fragment after view creation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for proper edge-to-edge display
        WindowInsetsHelper.applyWindowInsets(binding.root, binding.appBarLayout)

        setupUI()
        setupObservers()
    }

    // Sets up all user interface components
    private fun setupUI() {
        setupToolbar()
        setupSubmitButton()

        // Load property details
        viewModel.loadPropertyDetails()
    }

    // Configures the toolbar with navigation
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    // Sets up the submit button with form validation
    private fun setupSubmitButton() {
        binding.submitButton.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val phone = binding.phoneInput.text.toString()
            val message = binding.messageInput.text.toString()

            if (validateInputs(name, email, phone, message)) {
                viewModel.submitRequest(name, email, phone.ifBlank { null }, message)
            }
        }
    }

    // Validates form inputs and shows error messages
    private fun validateInputs(name: String, email: String, phone: String, message: String): Boolean {
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

        // Phone is optional, but validate format if provided
        if (phone.isNotBlank() && !android.util.Patterns.PHONE.matcher(phone).matches()) {
            binding.phoneInputLayout.error = "Invalid phone format"
            isValid = false
        } else {
            binding.phoneInputLayout.error = null
        }

        if (message.isBlank()) {
            binding.messageInputLayout.error = "Message is required"
            isValid = false
        } else {
            binding.messageInputLayout.error = null
        }

        return isValid
    }

    // Sets up observers for ViewModel state changes
    private fun setupObservers() {
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

    // Shows loading state while processing request
    private fun showLoading() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.submitButton.isEnabled = false
    }

    // Displays property details in the form
    private fun showPropertyDetails(state: RequestFormUiState.PropertyLoaded) {
        binding.loadingIndicator.visibility = View.GONE
        binding.submitButton.isEnabled = true

        binding.propertyTitle.text = state.property.title
        binding.propertyAddress.text = state.property.address

        // Display landlord information
        binding.landlordName.text = if (!state.property.landlordName.isNullOrEmpty()) {
            "Landlord: ${state.property.landlordName}"
        } else {
            "Landlord information not available"
        }
    }

    // Handles successful request submission
    private fun handleSuccess() {
        binding.loadingIndicator.visibility = View.GONE
        binding.submitButton.isEnabled = true

        // Show success message for contact request submission
        Snackbar.make(
            binding.root,
            "Contact request submitted successfully!",
            Snackbar.LENGTH_LONG
        ).apply {
            setAction("OK") {
                // Navigate back when user acknowledges
                findNavController().navigateUp()
            }
            show()
        }

        // Auto-navigate back after a delay if user doesn't interact
        binding.root.postDelayed({
            if (isAdded && !findNavController().currentDestination?.id.toString().contains("property_details")) {
                findNavController().navigateUp()
            }
        }, 3000) // 3 seconds delay
    }

    // Shows error message when request submission fails
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.submitButton.isEnabled = true

        // Show error message with retry option
        Snackbar.make(binding.root, "Failed to submit contact request: $message", Snackbar.LENGTH_LONG).apply {
            setAction("RETRY") {
                // Allow user to retry by clicking the submit button again
                // The form data is still there, so they can just click submit
            }
            show()
        }
    }

    // Cleans up view binding when fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Creates a new instance of RequestFormFragment
        fun newInstance() = RequestFormFragment()
    }
}
