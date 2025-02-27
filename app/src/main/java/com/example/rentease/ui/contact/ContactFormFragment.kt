package com.example.rentease.ui.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.rentease.R
import com.example.rentease.databinding.FragmentContactFormBinding
import com.example.rentease.ui.base.BaseFragment
import kotlinx.coroutines.launch

/**
 * ContactFormFragment handles the contact form functionality.
 * It replaces the ContactFormActivity in the fragment-based architecture.
 */
class ContactFormFragment : BaseFragment<FragmentContactFormBinding>() {
    
    private val args: ContactFormFragmentArgs by navArgs()
    private val propertyId: Int by lazy { args.propertyId }
    
    private val viewModel: ContactFormViewModel by viewModels {
        ContactFormViewModel.Factory(requireActivity().application, propertyId)
    }
    
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentContactFormBinding {
        return FragmentContactFormBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        setupToolbar()
        setupSubmitButton()
    }
    
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }
    
    private fun setupSubmitButton() {
        binding.submitButton.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val subject = binding.subjectInput.text.toString()
            val message = binding.messageInput.text.toString()
            
            if (name.isBlank() || email.isBlank() || subject.isBlank() || message.isBlank()) {
                Toast.makeText(requireContext(), R.string.error_all_fields_required, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            viewModel.submitContactForm(
                name = name,
                email = email,
                subject = subject,
                message = message
            )
        }
    }
    
    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ContactFormUiState.Initial -> showInitialState()
                        is ContactFormUiState.Loading -> showLoadingState()
                        is ContactFormUiState.Success -> handleSuccess()
                        is ContactFormUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun showInitialState() {
        binding.loadingIndicator.visibility = View.GONE
        binding.submitButton.isEnabled = true
    }
    
    private fun showLoadingState() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.submitButton.isEnabled = false
    }
    
    private fun handleSuccess() {
        binding.loadingIndicator.visibility = View.GONE
        
        // Show success message
        Toast.makeText(
            requireContext(),
            R.string.message_contact_form_sent,
            Toast.LENGTH_LONG
        ).show()
        
        // Navigate back
        findNavController().popBackStack()
    }
    
    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.submitButton.isEnabled = true
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    companion object {
        fun newInstance(propertyId: Int = -1) = ContactFormFragment().apply {
            arguments = Bundle().apply {
                putInt("propertyId", propertyId)
            }
        }
    }
}
