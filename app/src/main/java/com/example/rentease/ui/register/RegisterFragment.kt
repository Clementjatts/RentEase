package com.example.rentease.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.rentease.R
import com.example.rentease.auth.UserType
import com.example.rentease.databinding.FragmentRegisterBinding
import com.example.rentease.ui.base.BaseFragment
import kotlinx.coroutines.launch

/**
 * RegisterFragment handles user registration.
 * It replaces the RegisterActivity in the fragment-based architecture.
 */
class RegisterFragment : BaseFragment<FragmentRegisterBinding>() {
    
    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModel.Factory(requireActivity().application)
    }
    
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRegisterBinding {
        return FragmentRegisterBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
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
            requireActivity().onBackPressed()
        }
    }
    
    private fun setupUserTypeDropdown() {
        val userTypes = arrayOf(
            getString(R.string.user_type_tenant),
            getString(R.string.user_type_landlord)
        )
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            userTypes
        )
        
        binding.userTypeDropdown.setAdapter(adapter)
        binding.userTypeDropdown.setText(userTypes[0], false)
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
                userType = userType
            )
        }
    }
    
    private fun getUserTypeFromDropdown(): UserType {
        return when (binding.userTypeDropdown.text.toString()) {
            getString(R.string.user_type_landlord) -> UserType.LANDLORD
            else -> UserType.TENANT
        }
    }
    
    private fun setupLoginPrompt() {
        binding.loginPrompt.setOnClickListener {
            // Navigate back to login screen using NavigationHelper
            com.example.rentease.ui.navigation.NavigationHelper.navigateToLogin(findNavController())
        }
    }
    
    override fun setupObservers() {
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
        
        // Navigate to the login screen
        findNavController().popBackStack()
        
        // Reset the state so we don't navigate again if we come back to this fragment
        viewModel.resetState()
    }
    
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.registerButton.isEnabled = true
        binding.loginPrompt.isEnabled = true
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    companion object {
        fun newInstance() = RegisterFragment()
    }
}
