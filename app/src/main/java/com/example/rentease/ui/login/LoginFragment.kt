package com.example.rentease.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.rentease.R
import com.example.rentease.databinding.FragmentLoginBinding
import com.example.rentease.ui.base.BaseFragment
import kotlinx.coroutines.launch

/**
 * LoginFragment handles user authentication.
 * It replaces the LoginActivity in the fragment-based architecture.
 */
class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(requireActivity().application)
    }
    
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.login(email, password)
        }
        
        binding.registerButton.setOnClickListener {
            // Navigate to register screen using NavigationHelper
            com.example.rentease.ui.navigation.NavigationHelper.navigateToRegister(findNavController())
        }
    }
    
    override fun setupObservers() {
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
    
    private fun showInitialState() {
        binding.loadingIndicator.visibility = View.GONE
        binding.loginButton.isEnabled = true
        binding.registerButton.isEnabled = true
    }
    
    private fun showLoadingState() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false
        binding.registerButton.isEnabled = false
    }
    
    private fun handleLoginSuccess() {
        binding.loadingIndicator.visibility = View.GONE
        
        // Get the user type from the view model
        val userType = viewModel.getUserType()
        
        // Navigate to the appropriate dashboard based on user type
        com.example.rentease.ui.navigation.NavigationHelper.navigateToDashboard(findNavController(), userType)
        
        // Reset the state so we don't navigate again if we come back to this fragment
        viewModel.resetState()
    }
    
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.loginButton.isEnabled = true
        binding.registerButton.isEnabled = true
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    companion object {
        fun newInstance() = LoginFragment()
    }
}
