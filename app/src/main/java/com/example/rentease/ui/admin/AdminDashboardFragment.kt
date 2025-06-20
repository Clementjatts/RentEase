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
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.rentease.R
import com.example.rentease.auth.AuthManager
import com.example.rentease.databinding.FragmentAdminDashboardBinding
import com.example.rentease.ui.helpers.WindowInsetsHelper

// Admin dashboard fragment that displays management options for landlords and properties
class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var authManager: AuthManager

    // Initializes the fragment and sets up authentication manager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager.getInstance(requireContext())
    }

    // Creates and returns the view for this fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up the UI components after view creation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for proper edge-to-edge display
        WindowInsetsHelper.applyWindowInsets(binding.root, binding.appBarLayout)

        setupUI()
    }

    // Sets up all UI components including toolbar, menu, and buttons
    private fun setupUI() {
        setupToolbar()
        setupMenu()
        setupButtons()
    }

    // Configures the toolbar for the admin dashboard
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
    }

    // Sets up the options menu with web portal access
    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_dashboard, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_web_portal -> {
                        findNavController().navigate(R.id.action_adminDashboardFragment_to_webViewFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    // Sets up click listeners for dashboard buttons
    private fun setupButtons() {
        // Manage Landlords Button - Direct navigation to landlord management
        binding.manageLandlordsButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_landlordManagementFragment)
        }

        // Manage Properties Button - Direct navigation to property management
        binding.managePropertiesButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_propertyManagementFragment)
        }

        // Logout Button
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    // Shows a confirmation dialog before logging out
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_logout)
            .setMessage(R.string.message_confirm_logout)
            .setPositiveButton(R.string.button_logout) { _, _ ->
                authManager.logout()
                findNavController().navigate(R.id.action_global_loginFragment)
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    // Cleans up view binding when the view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Creates a new instance of AdminDashboardFragment
        fun newInstance() = AdminDashboardFragment()
    }
}
