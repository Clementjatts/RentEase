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
        setHasOptionsMenu(true)
        authManager = AuthManager.getInstance(requireContext())
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
        // Manage Users Button
        binding.manageUsersButton.setOnClickListener {
            NavigationHelper.navigateToUserManagement(findNavController())
        }
        
        // Manage Properties Button
        binding.managePropertiesButton.setOnClickListener {
            // Navigate to property management screen
            // This could be a different version of the property management screen
            // that shows all properties from all landlords
            NavigationHelper.navigateToPropertyList(findNavController())
        }
        
        // View Statistics Button
        binding.viewStatisticsButton.setOnClickListener {
            // Navigate to statistics screen
            // This could be a new screen that shows system statistics
            // For now, just show a toast message
            showNotImplementedDialog("Statistics")
        }
    }
    
    private fun showNotImplementedDialog(feature: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Feature Not Implemented")
            .setMessage("The $feature feature is not implemented yet.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_admin_dashboard, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            R.id.action_profile -> {
                NavigationHelper.navigateToProfile(findNavController())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
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
