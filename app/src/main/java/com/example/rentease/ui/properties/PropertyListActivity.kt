package com.example.rentease.ui.properties

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentease.R
import com.example.rentease.data.model.Property
import com.example.rentease.databinding.ActivityPropertyListBinding
import com.example.rentease.ui.contact.ContactFormActivity
import com.example.rentease.ui.details.PropertyDetailsActivity
import com.example.rentease.ui.login.LoginActivity
import com.example.rentease.ui.profile.ProfileActivity
import com.example.rentease.ui.propertyform.PropertyFormActivity
import com.example.rentease.ui.propertylist.SortBottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class PropertyListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPropertyListBinding
    private lateinit var adapter: PropertyAdapter

    private val viewModel: PropertyListViewModel by viewModels {
        PropertyListViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupSearch()
        setupFilterChips()
        setupSortButton()
        setupFab()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        adapter = PropertyAdapter(
            onItemClick = { property ->
                startActivity(PropertyDetailsActivity.createIntent(this, property.id))
            },
            onContactClick = { property ->
                startActivity(ContactFormActivity.createIntent(this, property.id))
            }
        )

        binding.propertiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PropertyListActivity)
            adapter = this@PropertyListActivity.adapter
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
        ).show(supportFragmentManager, "sort_dialog")
    }

    private fun setupFab() {
        binding.addPropertyFab.setOnClickListener {
            startActivity(PropertyFormActivity.createIntent(this))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
    }

    private fun showEmpty() {
        binding.propertiesRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
    }

    private fun showProperties(properties: List<Property>) {
        binding.propertiesRecyclerView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        adapter.submitList(properties)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.retry)) {
                viewModel.loadProperties()
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_property_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(ProfileActivity.createIntent(this))
                true
            }
            R.id.action_logout -> {
                startActivity(LoginActivity.createIntent(this))
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, PropertyListActivity::class.java)
    }
}
