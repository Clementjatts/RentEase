package com.example.rentease.ui.propertylist

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
import com.example.rentease.auth.AuthManager
import com.example.rentease.auth.UserType
import com.example.rentease.data.model.Property
import com.example.rentease.databinding.FragmentPropertyListBinding
import com.example.rentease.ui.helpers.WindowInsetsHelper
import kotlinx.coroutines.launch

// Fragment that displays the list of available properties as the main screen
class PropertyListFragment : Fragment() {

    private var _binding: FragmentPropertyListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PropertyListViewModel by viewModels {
        PropertyListViewModel.Factory(requireActivity().application)
    }

    private lateinit var propertyAdapter: PropertyListAdapter
    private lateinit var authManager: AuthManager

    // Creates and returns the fragment's view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropertyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up UI components and observers after view creation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize AuthManager
        authManager = AuthManager.getInstance(requireContext())

        // Apply window insets for proper edge-to-edge display
        WindowInsetsHelper.applyWindowInsets(binding.root, binding.appBarLayout)

        setupUI()
        setupObservers()
        updateUIBasedOnAuthState()
    }

    // Configures all UI components
    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupLoginButton()
        setupFab()
    }

    // Configures the toolbar for the fragment
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
    }

    // Sets up the RecyclerView with adapter and layout manager
    private fun setupRecyclerView() {
        propertyAdapter = PropertyListAdapter { property ->
            // Navigate to property details
            val action = PropertyListFragmentDirections
                .actionPropertyListFragmentToPropertyDetailsFragment(property.id)
            findNavController().navigate(action)
        }

        binding.propertiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = propertyAdapter
        }
    }

    // Configures pull-to-refresh functionality
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Trigger property refresh when user pulls down
            viewModel.refreshProperties()
        }
    }

    // Sets up the login button click listener
    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }

    // Configures the floating action button for adding properties
    private fun setupFab() {
        binding.addPropertyFab.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("propertyId", -1)
            }
            findNavController().navigate(R.id.action_global_propertyFormFragment, bundle)
        }
    }

    // Updates UI elements based on current authentication state
    private fun updateUIBasedOnAuthState() {
        if (authManager.isLoggedIn) {
            // User is logged in - hide login button
            binding.loginButton.visibility = View.GONE

            // Show FAB for landlords and admins
            when (authManager.userType) {
                UserType.LANDLORD, UserType.ADMIN -> {
                    binding.addPropertyFab.visibility = View.VISIBLE
                }
                else -> {
                    binding.addPropertyFab.visibility = View.GONE
                }
            }
        } else {
            // User is not logged in - show login button, hide FAB
            binding.loginButton.visibility = View.VISIBLE
            binding.addPropertyFab.visibility = View.GONE
        }
    }

    // Sets up observers for ViewModel state changes
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PropertyListUiState.Loading -> showLoading()
                        is PropertyListUiState.Success -> showProperties(state.properties)
                        is PropertyListUiState.Error -> showError(state.message)
                    }
                }
            }
        }

        // Load properties when the fragment is created
        viewModel.loadProperties()
    }

    // Shows loading state with appropriate indicators
    private fun showLoading() {
        // Only show the center loading indicator if we don't have data yet
        // If we're refreshing, the SwipeRefreshLayout will show its own indicator
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.loadingIndicator.visibility = View.VISIBLE
            binding.propertiesRecyclerView.visibility = View.GONE
        }
        binding.emptyView.visibility = View.GONE
    }

    // Displays the list of properties or empty state
    private fun showProperties(properties: List<Property>) {
        // Hide all loading indicators
        binding.loadingIndicator.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false

        if (properties.isEmpty()) {
            binding.propertiesRecyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.propertiesRecyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
            propertyAdapter.submitList(properties)
        }
    }

    // Shows error state with message
    private fun showError(message: String) {
        // Hide all loading indicators
        binding.loadingIndicator.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false

        // Show simple error message in empty view (no complex error UI needed)
        binding.propertiesRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.emptyView.text = message
    }

    // Updates UI when returning from other screens
    override fun onResume() {
        super.onResume()
        // Update UI when returning from other screens (like login)
        updateUIBasedOnAuthState()
    }

    // Cleans up view binding when fragment view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Creates a new instance of PropertyListFragment
        fun newInstance() = PropertyListFragment()
    }
} 