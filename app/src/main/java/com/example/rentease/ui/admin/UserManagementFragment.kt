package com.example.rentease.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentease.R
import com.example.rentease.databinding.FragmentUserManagementBinding
import com.example.rentease.ui.base.BaseFragment
import kotlinx.coroutines.launch

/**
 * UserManagementFragment displays the user management screen for admins.
 * It replaces the UserManagementActivity in the fragment-based architecture.
 */
class UserManagementFragment : BaseFragment<FragmentUserManagementBinding>() {
    
    private val viewModel: UserManagementViewModel by viewModels {
        UserManagementViewModel.Factory(requireActivity().application)
    }
    
    private lateinit var landlordAdapter: LandlordAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupBackNavigation()
    }
    
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_user_management, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_refresh -> {
                        viewModel.loadLandlords()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
    
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserManagementBinding {
        return FragmentUserManagementBinding.inflate(inflater, container, false)
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
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        landlordAdapter = LandlordAdapter(
            onApproveClick = { landlord ->
                viewModel.approveLandlord(landlord.id)
            },
            onRejectClick = { landlord ->
                showRejectConfirmation(landlord.id)
            }
        )
        
        binding.landlordRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = landlordAdapter
        }
    }
    
    private fun setupAddButton() {
        binding.addLandlordButton.setOnClickListener {
            // Show dialog to add a new landlord
            showAddLandlordDialog()
        }
    }
    
    private fun showAddLandlordDialog() {
        // This would typically show a dialog to add a new landlord
        // For now, just show a not implemented dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Feature Not Implemented")
            .setMessage("The Add Landlord feature is not implemented yet.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showRejectConfirmation(landlordId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Reject Landlord")
            .setMessage("Are you sure you want to reject this landlord?")
            .setPositiveButton("Reject") { _, _ ->
                viewModel.rejectLandlord(landlordId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UserManagementUiState.Loading -> showLoading()
                        is UserManagementUiState.Success -> showLandlords(state)
                        is UserManagementUiState.Error -> showError(state.message)
                    }
                }
            }
        }
        
        // Load landlords when the fragment is created
        viewModel.loadLandlords()
    }
    
    private fun showLoading() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.landlordRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
    }
    
    private fun showLandlords(state: UserManagementUiState.Success) {
        binding.loadingIndicator.visibility = View.GONE
        
        if (state.landlords.isEmpty()) {
            binding.landlordRecyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.landlordRecyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
            landlordAdapter.submitList(state.landlords)
        }
    }
    
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.landlordRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.emptyView.text = message
    }
    
    // Deprecated menu methods removed and replaced with MenuProvider in setupMenu()
    
    companion object {
        fun newInstance() = UserManagementFragment()
    }
}
