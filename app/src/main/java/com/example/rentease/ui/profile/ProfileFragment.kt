package com.example.rentease.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.rentease.R
import com.example.rentease.auth.AuthManager
import com.example.rentease.auth.UserType
import com.example.rentease.databinding.DialogChangePasswordBinding
import com.example.rentease.databinding.FragmentProfileBinding
import com.example.rentease.ui.base.BaseFragment
import kotlinx.coroutines.launch

/**
 * ProfileFragment handles user profile management.
 * It replaces the ProfileActivity in the fragment-based architecture.
 */
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModel.Factory(requireActivity().application)
    }
    
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
    }
    
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_profile, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_logout -> {
                        logout()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    override fun setupUI() {
        setupToolbar()
        setupSaveButton()
        setupChangePasswordButton()
    }
    
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
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
    
    private fun setupChangePasswordButton() {
        binding.changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }
    }
    
    private fun showChangePasswordDialog() {
        val dialogBinding = DialogChangePasswordBinding.inflate(layoutInflater)
        
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_change_password)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.button_save) { _, _ ->
                val currentPassword = dialogBinding.currentPasswordInput.text.toString()
                val newPassword = dialogBinding.newPasswordInput.text.toString()
                val confirmPassword = dialogBinding.confirmNewPasswordInput.text.toString()
                
                if (newPassword != confirmPassword) {
                    Toast.makeText(
                        requireContext(),
                        R.string.error_passwords_dont_match,
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }
                
                viewModel.changePassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }
    
    override fun setupObservers() {
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
    
    private fun showInitialState() {
        binding.loadingIndicator.visibility = View.GONE
        binding.saveButton.isEnabled = true
        binding.changePasswordButton.isEnabled = true
    }
    
    private fun showLoadingState() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.saveButton.isEnabled = false
        binding.changePasswordButton.isEnabled = false
    }
    
    private fun handleSuccess(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.saveButton.isEnabled = true
        binding.changePasswordButton.isEnabled = true
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.saveButton.isEnabled = true
        binding.changePasswordButton.isEnabled = true
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun showUserData(state: ProfileUiState.UserData) {
        binding.fullNameInput.setText(state.fullName)
        binding.emailInput.setText(state.email)
        binding.phoneInput.setText(state.phone)
        
        binding.usernameText.text = getString(R.string.profile_username, state.username)
        
        // Convert data model UserType to auth UserType for comparison
        val authUserType = when (state.userType.name) {
            "ADMIN" -> com.example.rentease.auth.UserType.ADMIN
            "LANDLORD" -> com.example.rentease.auth.UserType.LANDLORD
            else -> null
        }
        
        val userTypeString = when (authUserType) {
            com.example.rentease.auth.UserType.ADMIN -> getString(R.string.user_type_admin)
            com.example.rentease.auth.UserType.LANDLORD -> getString(R.string.user_type_landlord)
            null -> "Unknown"
        }
        binding.userTypeText.text = getString(R.string.profile_user_type, userTypeString)
        
        binding.joinDateText.text = getString(R.string.profile_join_date, state.joinDate)
    }
    
    private fun logout() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_logout)
            .setMessage(R.string.message_confirm_logout)
            .setPositiveButton(R.string.button_logout) { _, _ ->
                AuthManager.getInstance(requireContext()).logout()
                
                // Navigate to login screen
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }
    
    companion object {
        fun newInstance() = ProfileFragment()
    }
}
