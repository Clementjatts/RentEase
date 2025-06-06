package com.example.rentease.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.rentease.R
import com.example.rentease.auth.UserType
import com.example.rentease.databinding.FragmentRegisterBinding
import com.example.rentease.ui.utils.WindowInsetsHelper
import kotlinx.coroutines.launch

/**
 * RegisterFragment handles user registration.
 * Simplified implementation without BaseFragment complexity.
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModel.Factory(requireActivity().application)
    }

    // Flag to determine if this fragment was opened by an admin
    private var isFromAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the argument
        arguments?.let {
            isFromAdmin = it.getBoolean("isFromAdmin", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
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
        setupUserTypeDropdown()
        setupRegisterButton()
        setupLoginPrompt()
    }

    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupUserTypeDropdown() {
        // Define user types based on who's accessing the screen
        val userTypes = if (isFromAdmin) {
            // Admin can create both landlord and admin accounts
            arrayOf(
                getString(R.string.user_type_landlord),
                getString(R.string.user_type_admin)
            )
        } else {
            // Regular users can only create landlord accounts
            arrayOf(getString(R.string.user_type_landlord))
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            userTypes
        )

        binding.userTypeDropdown.setAdapter(adapter)
        binding.userTypeDropdown.setText(userTypes[0], false)

        // If not from admin, disable the dropdown to prevent changes
        binding.userTypeDropdown.isEnabled = isFromAdmin
    }

    private fun setupRegisterButton() {
        binding.registerButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val fullName = binding.fullNameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val phone = binding.phoneInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()
            val userType = getUserTypeFromDropdown()

            viewModel.register(
                username = username,
                fullName = fullName,
                email = email,
                phone = phone,
                password = password,
                confirmPassword = confirmPassword,
                userType = userType,
                isFromAdmin = isFromAdmin
            )
        }
    }

    private fun getUserTypeFromDropdown(): UserType {
        return when (binding.userTypeDropdown.text.toString()) {
            getString(R.string.user_type_admin) -> UserType.ADMIN
            else -> UserType.LANDLORD // Default to LANDLORD
        }
    }

    private fun setupLoginPrompt() {
        binding.loginPrompt.setOnClickListener {
            // Direct navigation to login screen
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is RegisterUiState.Initial -> showInitialState()
                        is RegisterUiState.Loading -> showLoadingState()
                        is RegisterUiState.Success -> handleRegisterSuccess()
                        is RegisterUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showInitialState() {
        binding.loadingIndicator.visibility = View.GONE
        binding.registerButton.isEnabled = true
        binding.loginPrompt.isEnabled = true
    }

    private fun showLoadingState() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.registerButton.isEnabled = false
        binding.loginPrompt.isEnabled = false
    }

    private fun handleRegisterSuccess() {
        binding.loadingIndicator.visibility = View.GONE

        // Show success message
        Toast.makeText(
            requireContext(),
            getString(R.string.message_registration_success),
            Toast.LENGTH_LONG
        ).show()

        // Navigate based on who initiated the registration - direct navigation
        if (isFromAdmin) {
            // Admin created a user - navigate back to admin dashboard to preserve admin session
            findNavController().navigate(R.id.action_global_adminDashboardFragment)
        } else {
            // Regular user registration - navigate to login screen
            findNavController().navigate(R.id.action_global_loginFragment)
        }

        // Reset the state so we don't navigate again if we come back to this fragment
        viewModel.resetState()
    }

    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.registerButton.isEnabled = true
        binding.loginPrompt.isEnabled = true

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = RegisterFragment()
    }
}
