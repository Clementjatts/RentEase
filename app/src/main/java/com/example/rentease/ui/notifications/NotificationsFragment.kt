package com.example.rentease.ui.notifications

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
import com.example.rentease.data.model.Request
import com.example.rentease.databinding.FragmentNotificationsBinding
import com.example.rentease.ui.helpers.WindowInsetsHelper
import kotlinx.coroutines.launch

// Fragment for displaying landlord notifications
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by viewModels {
        NotificationsViewModel.Factory(requireActivity().application)
    }

    private lateinit var notificationAdapter: NotificationAdapter

    // Creates the fragment's view hierarchy
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Initializes the fragment after view creation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for proper edge-to-edge display
        WindowInsetsHelper.applyWindowInsets(binding.root, binding.appBarLayout)

        setupUI()
        setupObservers()

        // Load notifications when fragment is created
        viewModel.loadNotifications()
    }

    // Sets up all user interface components
    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
    }

    // Configures the toolbar with navigation
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle back button click
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    // Configures the RecyclerView with adapter and layout manager
    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { request ->
            // Mark notification as read when clicked
            viewModel.markAsRead(request.id)
        }

        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }

    // Sets up pull-to-refresh functionality
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadNotifications()
        }
    }
    
    // Sets up observers for ViewModel state changes
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is NotificationsUiState.Loading -> showLoading()
                        is NotificationsUiState.Success -> showNotifications(state.requests)
                        is NotificationsUiState.Empty -> showEmptyState()
                        is NotificationsUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    // Shows loading state while notifications are being fetched
    private fun showLoading() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.notificationsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false
    }

    // Displays the list of notifications
    private fun showNotifications(requests: List<Request>) {
        binding.loadingIndicator.visibility = View.GONE
        binding.notificationsRecyclerView.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false

        notificationAdapter.submitList(requests)
    }

    // Shows empty state when no notifications are available
    private fun showEmptyState() {
        binding.loadingIndicator.visibility = View.GONE
        binding.notificationsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.swipeRefreshLayout.isRefreshing = false
    }

    // Shows error state when notification loading fails
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.notificationsRecyclerView.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false

        showEmptyState()
    }

    // Cleans up view binding when fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
