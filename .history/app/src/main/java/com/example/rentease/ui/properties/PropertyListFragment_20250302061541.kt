package com.example.rentease.ui.properties

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentease.R
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.model.Property
import com.example.rentease.databinding.FragmentPropertyListBinding
import com.example.rentease.ui.base.BaseFragment
import com.example.rentease.ui.navigation.NavigationHelper
import com.example.rentease.ui.propertylist.SortBottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class PropertyListFragment : BaseFragment<FragmentPropertyListBinding>() {
    private lateinit var adapter: PropertyAdapter

    private val viewModel: PropertyListViewModel by viewModels {
        PropertyListViewModel.Factory(requireActivity().application)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPropertyListBinding {
        return FragmentPropertyListBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
    }
    
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_property_list, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_login -> {
                        // Navigate to login fragment using NavigationHelper
                        NavigationHelper.navigateToLogin(findNavController())
                        true
                    }
                    R.id.action_register -> {
                        // Navigate to register fragment using NavigationHelper
                        NavigationHelper.navigateToRegister(findNavController())
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupSearch()
        setupFilterChips()
        setupSortButton()
        setupFab()
        
        // Check authentication status and update UI accordingly
        updateUIBasedOnAuthStatus()
    }
    
    private fun setupToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
    }
    
    private fun updateUIBasedOnAuthStatus() {
        val isLoggedIn = AuthManager.getInstance(requireContext()).isLoggedIn
        // Hide FAB if user is not logged in
        binding.addPropertyFab.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        adapter = PropertyAdapter(
            onItemClick = { property ->
                // Use NavigationHelper to navigate to PropertyDetailsFragment
                NavigationHelper.navigateToPropertyDetails(findNavController(), property.id)
            },
            onContactClick = { property ->
                // Use NavigationHelper to navigate to RequestFormFragment instead of ContactFormFragment
                NavigationHelper.navigateToRequestForm(
                    findNavController(),
                    propertyId = property.id,
                    landlordId = property.landlordId
                )
            }
        )

        binding.propertiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PropertyListFragment.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupFilterChips() {
        binding.filterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedChip = group.findViewById<Chip>(checkedIds.firstOrNull() ?: R.id.chipAll)
            val filter = when (selectedChip.id) {
                R.id.chipPrice -> PropertyFilter.PRICE
                R.id.chipLocation -> PropertyFilter.LOCATION
                else -> PropertyFilter.ALL
            }
            viewModel.setFilter(filter)
        }
    }

    private fun setupSortButton() {
        binding.sortButton.setOnClickListener {
            showSortDialog()
        }
    }
    
    private fun showSortDialog() {
        SortBottomSheetDialog.newInstance(
            currentOption = viewModel.getCurrentSortOption(),
            onOptionSelected = { option ->
                viewModel.setSortOption(option)
            }
        ).show(parentFragmentManager, "sort_dialog")
    }

    private fun setupFab() {
        binding.addPropertyFab.setOnClickListener {
            // Use NavigationHelper to navigate to PropertyFormFragment
            NavigationHelper.navigateToPropertyForm(findNavController())
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            PropertyListUiState.Loading -> showLoading()
                            PropertyListUiState.Empty -> showEmpty()
                            is PropertyListUiState.Success -> showProperties(state.properties)
                            is PropertyListUiState.Error -> showError(state.message)
                        }
                    }
                }

                launch {
                    viewModel.isRefreshing.collect { isRefreshing ->
                        binding.swipeRefreshLayout.isRefreshing = isRefreshing
                    }
                }

                launch {
                    viewModel.filteredProperties.collect { properties ->
                        if (properties.isEmpty() && viewModel.uiState.value !is PropertyListUiState.Loading) {
                            showEmpty()
                        } else {
                            binding.propertiesRecyclerView.visibility = View.VISIBLE
                            binding.emptyView.visibility = View.GONE
                            adapter.submitList(properties)
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.propertiesRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.errorView.visibility = View.GONE
    }

    private fun showEmpty() {
        binding.propertiesRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.loadingIndicator.visibility = View.GONE
        binding.errorView.visibility = View.GONE
    }

    private fun showProperties(properties: List<Property>) {
        binding.propertiesRecyclerView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        binding.loadingIndicator.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        adapter.submitList(properties)
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.propertiesRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.loadingIndicator.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
        
        binding.retryButton.setOnClickListener {
            viewModel.loadProperties()
        }
        
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.retry)) {
                viewModel.loadProperties()
            }
            .show()
    }

    // Public methods to be called from the activity
    fun setSearchQuery(query: String) {
        viewModel.setSearchQuery(query)
    }

    fun setFilter(filter: PropertyFilter) {
        viewModel.setFilter(filter)
    }

    fun setSortOption(option: com.example.rentease.ui.propertylist.SortBottomSheetDialog.SortOption) {
        viewModel.setSortOption(option)
    }

    fun refresh() {
        viewModel.refresh()
    }

    companion object {
        fun newInstance() = PropertyListFragment()
    }
}
