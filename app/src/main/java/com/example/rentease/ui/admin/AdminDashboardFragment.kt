package com.example.rentease.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rentease.R
import com.example.rentease.auth.AuthManager
import com.example.rentease.databinding.FragmentAdminDashboardBinding
import com.example.rentease.ui.helpers.WindowInsetsHelper

/**
 * AdminDashboardFragment displays the admin dashboard screen.
 * Simplified implementation without BaseFragment complexity.
 */
class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for proper edge-to-edge display
        WindowInsetsHelper.applyWindowInsets(binding.root, binding.appBarLayout)

        setupUI()
    }

    private fun setupUI() {
        setupToolbar()
        setupButtons()
    }

    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AdminDashboardFragment()
    }
}
