package com.example.rentease.ui.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentease.R
import com.example.rentease.databinding.FragmentPropertyManagementBinding
import com.example.rentease.ui.base.BaseFragment
import com.example.rentease.ui.navigation.NavigationHelper
import com.example.rentease.ui.properties.PropertyAdapter
import kotlinx.coroutines.launch

/**
 * PropertyManagementFragment displays the landlord's properties for management.
 * It replaces the PropertyManagementActivity in the fragment-based architecture.
 */
class PropertyManagementFragment : BaseFragment<FragmentPropertyManagementBinding>() {
    
    private val viewModel: PropertyManagementViewModel by viewModels {
        PropertyManagementViewModel.Factory(requireActivity().application)
    }
    
    private lateinit var propertyAdapter: PropertyAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPropertyManagementBinding {
        return FragmentPropertyManagementBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupAddButton()
    }
    
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        propertyAdapter = PropertyAdapter(
            onItemClick = { property ->
                NavigationHelper.navigateToPropertyForm(findNavController(), property.id)
            },
            onDeleteClick = { property ->
                viewModel.deleteProperty(property.id)
            }
        )
        
        binding.propertyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = propertyAdapter
        }
    }
    
    private fun setupAddButton() {
        binding.addPropertyButton.setOnClickListener {
            NavigationHelper.navigateToPropertyForm(findNavController())
        }
    }
    
    override fun setupObservers() {
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
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_property_management, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.loadProperties()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    companion object {
        fun newInstance() = PropertyManagementFragment()
    }
}
