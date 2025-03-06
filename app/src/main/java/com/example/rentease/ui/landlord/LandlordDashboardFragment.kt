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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
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
                menuInflater.inflate(R.menu.menu_landlord_dashboard, menu)
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
        fun newInstance() = LandlordDashboardFragment()
    }
}
