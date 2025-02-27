package com.example.rentease.ui.landlord

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
import com.example.rentease.databinding.FragmentLandlordDashboardBinding
import com.example.rentease.ui.base.BaseFragment
import com.example.rentease.ui.navigation.NavigationHelper

/**
 * LandlordDashboardFragment displays the landlord dashboard screen.
 * It replaces the LandlordDashboardActivity in the fragment-based architecture.
 */
class LandlordDashboardFragment : BaseFragment<FragmentLandlordDashboardBinding>() {
    
    private lateinit var authManager: AuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        authManager = AuthManager.getInstance(requireContext())
    }
    
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLandlordDashboardBinding {
        return FragmentLandlordDashboardBinding.inflate(inflater, container, false)
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
        // Add Property Button
        binding.addPropertyButton.setOnClickListener {
            NavigationHelper.navigateToPropertyForm(findNavController())
        }
        
        // Manage Properties Button
        binding.managePropertiesButton.setOnClickListener {
            NavigationHelper.navigateToPropertyManagement(findNavController())
        }
        
        // Edit Profile Button
        binding.editProfileButton.setOnClickListener {
            NavigationHelper.navigateToProfile(findNavController())
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_landlord_dashboard, menu)
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
        fun newInstance() = LandlordDashboardFragment()
    }
}
