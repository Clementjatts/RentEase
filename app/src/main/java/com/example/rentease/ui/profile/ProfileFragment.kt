package com.example.rentease.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.rentease.databinding.FragmentProfileBinding
import com.example.rentease.ui.helpers.WindowInsetsHelper
import kotlinx.coroutines.launch
import androidx.core.view.isGone

// ProfileFragment handles user profile management
class ProfileFragment : Fragment(), ChangePasswordDialog.PasswordChangeListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Store original values to track changes
    private var originalFullName = ""
    private var originalEmail = ""
    private var originalPhone = ""

    private val viewModel: ProfileViewModel by viewModels {
        // Get landlordId from arguments
        val landlordId = arguments?.getInt("landlordId", -1)?.takeIf { it > 0 }

        // Get the current user type to determine how to handle the landlordId
        val userType = com.example.rentease.auth.AuthManager.getInstance(requireContext()).userType

        // Determine the effective landlord ID based on user type and provided landlordId
        val effectiveLandlordId = when {
            // If a specific landlordId is provided (e.g., admin editing a landlord), use it
            landlordId != null -> landlordId

            // If no landlordId is provided and current user is a landlord viewing their own profile,
            // don't pass a landlordId (this will make ProfileViewModel use current user's profile)
            userType == com.example.rentease.auth.UserType.LANDLORD -> null

            // For other cases (admin viewing their own profile), don't pass a landlordId
            else -> null
        }

        ProfileViewModel.Factory(requireActivity().application, effectiveLandlordId)
    }

    // Creates the fragment's view hierarchy
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
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
        setupSaveButton()
        setupChangePasswordButton()
        setupFormChangeListeners()
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

    // Sets up the save button to update profile information
    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val fullName = binding.fullNameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val phone = binding.phoneInput.text.toString()

            viewModel.updateProfile(
                fullName = fullName,
                email = email,
                phone = phone
            )
        }
    }

    // Sets up the change password button to show dialog
    private fun setupChangePasswordButton() {
        binding.changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    // Sets up text change listeners to track form modifications
    private fun setupFormChangeListeners() {
        // Add text change listeners to track form modifications
        binding.fullNameInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateSaveButtonState()
            }
        })

        binding.emailInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateSaveButtonState()
            }
        })

        binding.phoneInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateSaveButtonState()
            }
        })
    }

    // Updates save button state based on form changes
    private fun updateSaveButtonState() {
        val currentFullName = binding.fullNameInput.text.toString()
        val currentEmail = binding.emailInput.text.toString()
        val currentPhone = binding.phoneInput.text.toString()

        val hasChanges = currentFullName != originalFullName ||
                currentEmail != originalEmail ||
                currentPhone != originalPhone

        // Only enable save button if there are changes and form is not in loading state
        binding.saveButton.isEnabled = hasChanges && binding.loadingIndicator.isGone
    }

    // Shows the change password dialog
    private fun showChangePasswordDialog() {
        val dialog = ChangePasswordDialog.newInstance()
        dialog.show(parentFragmentManager, ChangePasswordDialog.TAG)
    }

    // Sets up observers for ViewModel state changes
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is ProfileUiState.Initial -> showInitialState()
                            is ProfileUiState.Loading -> showLoadingState()
                            is ProfileUiState.Success -> handleSuccess(state.message)
                            is ProfileUiState.Error -> showError(state.message)
                            is ProfileUiState.UserData -> showUserData(state)
                        }
                    }
                }
            }
        }

        // Load user data when the fragment is created
        viewModel.loadUserData()
    }

    // Shows the initial state with enabled controls
    private fun showInitialState() {
        binding.loadingIndicator.visibility = View.GONE
        binding.changePasswordButton.isEnabled = true
        updateSaveButtonState()
    }

    // Shows loading state with disabled controls
    private fun showLoadingState() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.saveButton.isEnabled = false
        binding.changePasswordButton.isEnabled = false
    }

    // Handles successful profile update
    private fun handleSuccess(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.changePasswordButton.isEnabled = true
        updateSaveButtonState()

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    // Shows error message and re-enables controls
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.changePasswordButton.isEnabled = true
        updateSaveButtonState()

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    // Populates form fields with user data
    private fun showUserData(state: ProfileUiState.UserData) {
        // Hide loading indicator and enable change password button when user data is loaded
        binding.loadingIndicator.visibility = View.GONE
        binding.changePasswordButton.isEnabled = true

        // Store original values for change tracking
        originalFullName = state.fullName
        originalEmail = state.email
        originalPhone = state.phone

        // Set the user data in the form fields
        binding.fullNameInput.setText(state.fullName)
        binding.emailInput.setText(state.email)
        binding.phoneInput.setText(state.phone)

        // Update save button state based on changes
        updateSaveButtonState()

        // Account information section has been removed from the layout
    }

    // Handles password change success callback
    override fun onPasswordChanged() {
        Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_LONG).show()
    }

    // Cleans up view binding when fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Creates a new instance of ProfileFragment
        fun newInstance() = ProfileFragment()
    }
}
