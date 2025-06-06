package com.example.rentease.ui.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentease.R
import com.example.rentease.data.model.Property
import com.example.rentease.databinding.FragmentPropertyManagementBinding
import com.example.rentease.ui.utils.WindowInsetsHelper
import kotlinx.coroutines.launch

/**
 * PropertyManagementFragment displays the landlord's properties for management.
 * Simplified implementation without BaseFragment complexity.
 */
class PropertyManagementFragment : Fragment() {

    private var _binding: FragmentPropertyManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PropertyManagementViewModel by viewModels {
        PropertyManagementViewModel.Factory(requireActivity().application)
    }

    private lateinit var propertyAdapter: PropertyManagementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropertyManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for proper edge-to-edge display
        WindowInsetsHelper.applyWindowInsets(binding.root, binding.appBarLayout)

        setupUI()
        setupObservers()
        setupMenu()
        setupBackNavigation()
    }

    private fun setupMenu() {
        // Menu setup removed as refresh function is not needed
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )
    }

    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
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
        propertyAdapter = PropertyManagementAdapter(
            onItemClick = { property ->
                // Direct navigation to PropertyFormFragment for editing
                val bundle = Bundle().apply {
                    putInt("propertyId", property.id)
                }
                findNavController().navigate(R.id.action_global_propertyFormFragment, bundle)
            },
            onDeleteClick = { property ->
                showDeleteConfirmation(property)
            }
        )

        binding.propertyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = propertyAdapter
        }
    }

    private fun showDeleteConfirmation(property: Property) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Property")
            .setMessage("Are you sure you want to delete '${property.title}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteProperty(property.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is PropertyManagementUiState.Loading -> showLoading()
                        is PropertyManagementUiState.Success -> showProperties(state)
                        is PropertyManagementUiState.Error -> showError(state.message)
                    }
                }
            }
        }

        // Load properties when the fragment is created
        viewModel.loadProperties()
    }

    private fun showLoading() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.propertyRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
    }

    private fun showProperties(state: PropertyManagementUiState.Success) {
        binding.loadingIndicator.visibility = View.GONE

        if (state.properties.isEmpty()) {
            binding.propertyRecyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.propertyRecyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
            propertyAdapter.submitList(state.properties)
        }
    }

    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.propertyRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.emptyView.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PropertyManagementFragment()
    }
}
