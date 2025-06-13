package com.example.rentease.ui.landlord

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
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.rentease.R
import com.example.rentease.auth.AuthManager
import com.example.rentease.databinding.FragmentLandlordDashboardBinding
import com.example.rentease.di.RepositoryProvider
import com.example.rentease.ui.helpers.WindowInsetsHelper
import com.example.rentease.data.model.Result
import kotlinx.coroutines.launch

// LandlordDashboardFragment displays the landlord dashboard screen.
class LandlordDashboardFragment : Fragment() {

    private var _binding: FragmentLandlordDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var authManager: AuthManager
    private val userRepository by lazy { RepositoryProvider.provideUserRepository(requireActivity().application) }
    private val requestRepository by lazy { RepositoryProvider.provideRequestRepository(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandlordDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for proper edge-to-edge display
        WindowInsetsHelper.applyWindowInsets(binding.root, binding.appBarLayout)

        setupUI()
        loadNotificationCount()
    }

    private fun setupUI() {
        setupToolbar()
        setupMenu()
        setupButtons()
        setupNotificationIcon()
    }

    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_dashboard, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_web_portal -> {
                        findNavController().navigate(R.id.action_landlordDashboardFragment_to_webViewFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupButtons() {
        // Add Property Button - Direct navigation
        binding.addPropertyButton.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("propertyId", -1)
            }
            findNavController().navigate(R.id.action_global_propertyFormFragment, bundle)
        }

        // Manage Properties Button - Direct navigation
        binding.managePropertiesButton.setOnClickListener {
            findNavController().navigate(R.id.action_landlordDashboardFragment_to_propertyManagementFragment)
        }

        // Edit Profile Button
        binding.editProfileButton.setOnClickListener {
            navigateToProfileWithCorrectLandlordId()
        }

        // Logout Button
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun setupNotificationIcon() {
        binding.notificationContainer.setOnClickListener {
            findNavController().navigate(R.id.action_landlordDashboardFragment_to_notificationsFragment)
        }
    }

    private fun loadNotificationCount() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userId = authManager.getUserId().toIntOrNull()
                if (userId != null) {
                    val result = requestRepository.getUnreadCount(userId)
                    if (result.isSuccess) {
                        val count = (result as Result.Success<Int>).data
                        updateNotificationBadge(count)
                    }
                }
            } catch (e: Exception) {
                // Notification count is non-critical
            }
        }
    }

    private fun updateNotificationBadge(count: Int) {
        if (count > 0) {
            binding.notificationBadge.text = if (count > 99) "99+" else count.toString()
            binding.notificationBadge.visibility = View.VISIBLE
        } else {
            binding.notificationBadge.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh notification count when returning to dashboard
        loadNotificationCount()
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

    // Navigate to the profile fragment with the correct landlord ID
    private fun navigateToProfileWithCorrectLandlordId() {
        try {
            val userId = authManager.getUserId().toIntOrNull()
            if (userId != null && userId > 0) {
                // Direct navigation to profile with landlord ID (same as user ID)
                val bundle = Bundle().apply {
                    putInt("landlordId", userId)
                }
                findNavController().navigate(R.id.action_global_profileFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "Invalid user ID", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = LandlordDashboardFragment()
    }
}
