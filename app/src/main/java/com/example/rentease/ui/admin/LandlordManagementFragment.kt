package com.example.rentease.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentease.R
import com.example.rentease.data.model.User
import com.example.rentease.databinding.FragmentLandlordManagementBinding
import com.example.rentease.ui.helpers.WindowInsetsHelper
import kotlinx.coroutines.launch

// Fragment that displays and manages landlord accounts for admin users
class LandlordManagementFragment : Fragment() {

    private var _binding: FragmentLandlordManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LandlordManagementViewModel by viewModels {
        LandlordManagementViewModel.Factory(requireActivity().application)
    }

    private lateinit var landlordAdapter: LandlordAdapter

    // Creates and returns the view for this fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandlordManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up the UI components and observers after view creation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for proper edge-to-edge display
        WindowInsetsHelper.applyWindowInsets(binding.root, binding.appBarLayout)

        setupUI()
        setupObservers()
    }

    // Sets up all UI components including toolbar, RecyclerView, and FAB
    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
    }

    // Configures the toolbar with navigation support
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    // Sets up the RecyclerView with adapter and click handlers
    private fun setupRecyclerView() {
        landlordAdapter = LandlordAdapter(
            onEditClick = { user ->
                // Navigate to user profile for editing (landlord)
                val bundle = Bundle().apply {
                    putInt("landlordId", user.id)
                }
                findNavController().navigate(R.id.action_landlordManagementFragment_to_profileFragment, bundle)
            },
            onDeleteClick = { user ->
                // Handle landlord deletion
                showDeleteConfirmation(user)
            }
        )

        binding.landlordsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = landlordAdapter
        }
    }

    // Configures pull-to-refresh functionality
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Trigger landlord refresh when user pulls down
            viewModel.loadLandlords()
        }
    }

    // Sets up the floating action button for adding new landlords
    private fun setupFab() {
        binding.addLandlordFab.setOnClickListener {
            // Navigate to register screen for admin to create landlord
            val bundle = Bundle().apply {
                putBoolean("isFromAdmin", true)
            }
            findNavController().navigate(R.id.action_landlordManagementFragment_to_registerFragment, bundle)
        }
    }

    // Sets up observers for ViewModel state changes
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LandlordManagementUiState.Loading -> showLoading()
                        is LandlordManagementUiState.Success -> showLandlords(state.landlords)
                        is LandlordManagementUiState.Error -> showError(state.message)
                    }
                }
            }
        }

        // Load landlords when the fragment is created
        viewModel.loadLandlords()
    }

    // Shows loading state with appropriate indicators
    private fun showLoading() {
        // Only show the center loading indicator if we don't have data yet
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.loadingIndicator.visibility = View.VISIBLE
            binding.landlordsRecyclerView.visibility = View.GONE
        }
        binding.emptyView.visibility = View.GONE
    }

    // Displays the list of landlords or empty state
    private fun showLandlords(landlords: List<User>) {
        // Hide all loading indicators
        binding.loadingIndicator.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false

        if (landlords.isEmpty()) {
            binding.landlordsRecyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.landlordsRecyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
            landlordAdapter.submitList(landlords)
        }
    }

    // Shows error state with message
    private fun showError(message: String) {
        // Hide all loading indicators
        binding.loadingIndicator.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false

        // Show simple error message in empty view
        binding.landlordsRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.emptyView.text = message
    }

    // Shows confirmation dialog before deleting a landlord
    private fun showDeleteConfirmation(user: User) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Landlord")
            .setMessage("Are you sure you want to delete ${user.fullName ?: user.username}?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteLandlord(user.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Cleans up view binding when the view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
