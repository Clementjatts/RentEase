package com.example.rentease.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.rentease.R
import com.example.rentease.auth.AuthManager
import com.example.rentease.databinding.FragmentAdminDashboardBinding
import com.example.rentease.ui.base.BaseFragment
import com.example.rentease.ui.navigation.NavigationHelper

/**
 * AdminDashboardFragment displays the admin dashboard screen.
 * It replaces the AdminDashboardActivity in the fragment-based architecture.
 */
class AdminDashboardFragment : BaseFragment<FragmentAdminDashboardBinding>() {
    
    private lateinit var authManager: AuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager.getInstance(requireContext())
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
    }
    
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_admin_dashboard, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_logout -> {
                        showLogoutConfirmation()
                        true
                    }
                    R.id.action_profile -> {
                        NavigationHelper.navigateToProfile(findNavController())
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAdminDashboardBinding {
        return FragmentAdminDashboardBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        setupToolbar()
        setupButtons()
    }
    
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
    }
    
    private fun setupButtons() {
        // Manage Users Button - Make sure all buttons have the same size
        binding.manageUsersButton.setOnClickListener {
            NavigationHelper.navigateToUserManagement(findNavController())
        }
        
        // Manage Properties Button - Navigate to property management
        binding.managePropertiesButton.setOnClickListener {
            // Navigate to property management screen for admin
            // This shows all properties from all landlords
            NavigationHelper.navigateToPropertyManagement(findNavController())
        }
        
        // Statistics section removed as per requirements
    }
    
    private fun showNotImplementedDialog(feature: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Feature Not Implemented")
            .setMessage("The $feature feature is not implemented yet.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    // Deprecated menu methods removed and replaced with MenuProvider in setupMenu()
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_logout)
            .setMessage(R.string.message_confirm_logout)
            .setPositiveButton(R.string.button_logout) { _, _ ->
                authManager.logout()
                NavigationHelper.navigateToLogin(findNavController())
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }
    
    companion object {
        fun newInstance() = AdminDashboardFragment()
    }
}
