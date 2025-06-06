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
import com.example.rentease.notification.NotificationService
import com.example.rentease.ui.utils.WindowInsetsHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * RequestFormFragment handles contact requests for properties.
 * Simplified implementation without BaseFragment complexity.
 */
class RequestFormFragment : Fragment() {

    private var _binding: FragmentRequestFormBinding? = null
    private val binding get() = _binding!!

    private val args: RequestFormFragmentArgs by navArgs()

    private val viewModel: RequestFormViewModel by viewModels {
        RequestFormViewModel.Factory(requireActivity().application, args.propertyId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestFormBinding.inflate(inflater, container, false)
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
        binding.submitButton.isEnabled = true

        // Show notification
        NotificationService.showRequestSubmittedNotification(requireContext())

        // Show success message for email sending
        Snackbar.make(
            binding.root,
            "Email sent successfully to landlord! They will contact you directly.",
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

    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.submitButton.isEnabled = true

        // Show error message with retry option
        Snackbar.make(binding.root, "Failed to send email: $message", Snackbar.LENGTH_LONG).apply {
            setAction("RETRY") {
                // Allow user to retry by clicking the submit button again
                // The form data is still there, so they can just click submit
            }
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = RequestFormFragment()
    }
}
