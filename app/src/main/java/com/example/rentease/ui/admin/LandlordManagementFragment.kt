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
import com.example.rentease.data.model.Landlord
import com.example.rentease.databinding.FragmentLandlordManagementBinding
import com.example.rentease.ui.helpers.WindowInsetsHelper
import kotlinx.coroutines.launch

/**
 * LandlordManagementFragment displays and manages landlord accounts for admins.
 */
class LandlordManagementFragment : Fragment() {

    private var _binding: FragmentLandlordManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LandlordManagementViewModel by viewModels {
        LandlordManagementViewModel.Factory(requireActivity().application)
    }

    private lateinit var landlordAdapter: LandlordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandlordManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for proper edge-to-edge display
        WindowInsetsHelper.applyWindowInsets(binding.root, binding.appBarLayout)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
    }

    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        landlordAdapter = LandlordAdapter(
            onEditClick = { landlord ->
                // Navigate to landlord profile for editing
                val bundle = Bundle().apply {
                    putInt("landlordId", landlord.id)
                }
                findNavController().navigate(R.id.action_landlordManagementFragment_to_profileFragment, bundle)
            },
            onDeleteClick = { landlord ->
                // Handle landlord deletion
                showDeleteConfirmation(landlord)
            }
        )

        binding.landlordsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = landlordAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Trigger landlord refresh when user pulls down
            viewModel.loadLandlords()
        }
    }

    private fun setupFab() {
        binding.addLandlordFab.setOnClickListener {
            // Navigate to register screen for admin to create landlord
            val bundle = Bundle().apply {
                putBoolean("isFromAdmin", true)
            }
            findNavController().navigate(R.id.action_landlordManagementFragment_to_registerFragment, bundle)
        }
    }

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

    private fun showLoading() {
        // Only show the center loading indicator if we don't have data yet
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.loadingIndicator.visibility = View.VISIBLE
            binding.landlordsRecyclerView.visibility = View.GONE
        }
        binding.emptyView.visibility = View.GONE
    }

    private fun showLandlords(landlords: List<Landlord>) {
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

    private fun showError(message: String) {
        // Hide all loading indicators
        binding.loadingIndicator.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false

        // Show simple error message in empty view
        binding.landlordsRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.emptyView.text = message
    }

    private fun showDeleteConfirmation(landlord: Landlord) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Landlord")
            .setMessage("Are you sure you want to delete ${landlord.name}?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteLandlord(landlord.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
