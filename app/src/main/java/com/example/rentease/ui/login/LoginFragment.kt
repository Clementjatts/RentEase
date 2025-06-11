package com.example.rentease.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.rentease.R
import com.example.rentease.databinding.FragmentLoginBinding
import com.example.rentease.ui.helpers.WindowInsetsHelper
import kotlinx.coroutines.launch

// Login fragment that handles user authentication with simplified implementation
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(requireActivity().application)
    }

    // Creates and returns the fragment's view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up UI components and observers after view creation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for proper edge-to-edge display
        WindowInsetsHelper.applyWindowInsets(binding.root, binding.appBarLayout)

        setupUI()
        setupObservers()
    }

    // Configures UI components and click listeners
    private fun setupUI() {
        // Set up toolbar navigation icon click listener
        binding.toolbar.setNavigationOnClickListener {
            // Navigate back to property list - direct navigation
            findNavController().navigate(R.id.action_loginFragment_to_propertyListFragment)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.login(email, password)
        }

        binding.registerButton.setOnClickListener {
            // Direct navigation to register screen
            val bundle = Bundle().apply {
                putBoolean("isFromAdmin", false)
            }
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment, bundle)
        }
    }

    // Sets up observers for ViewModel state changes
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LoginUiState.Initial -> showInitialState()
                        is LoginUiState.Loading -> showLoadingState()
                        is LoginUiState.Success -> handleLoginSuccess()
                        is LoginUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    // Shows the initial UI state with enabled buttons
    private fun showInitialState() {
        binding.loadingIndicator.visibility = View.GONE
        binding.loginButton.isEnabled = true
        binding.registerButton.isEnabled = true
    }

    // Shows loading state with disabled buttons and progress indicator
    private fun showLoadingState() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false
        binding.registerButton.isEnabled = false
    }

    // Handles successful login by navigating to appropriate dashboard
    private fun handleLoginSuccess() {
        binding.loadingIndicator.visibility = View.GONE
        
        // Get the user type from the view model
        val userType = viewModel.getUserType()
        
        // Navigate to the appropriate dashboard based on user type - direct navigation
        when (userType) {
            com.example.rentease.auth.UserType.ADMIN ->
                findNavController().navigate(R.id.action_global_adminDashboardFragment)
            com.example.rentease.auth.UserType.LANDLORD ->
                findNavController().navigate(R.id.action_global_landlordDashboardFragment)
            null ->
                findNavController().navigate(R.id.action_global_propertyListFragment)
        }
        
        // Reset the state so we don't navigate again if we come back to this fragment
        viewModel.resetState()
    }

    // Displays error message and re-enables UI controls
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.loginButton.isEnabled = true
        binding.registerButton.isEnabled = true

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    // Cleans up view binding when fragment view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Creates a new instance of LoginFragment
        fun newInstance() = LoginFragment()
    }
}
