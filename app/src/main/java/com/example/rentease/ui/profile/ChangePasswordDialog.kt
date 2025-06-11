package com.example.rentease.ui.profile

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rentease.databinding.DialogChangePasswordBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

// ChangePasswordDialog handles password change functionality
class ChangePasswordDialog : DialogFragment() {
    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChangePasswordViewModel by viewModels {
        ChangePasswordViewModel.Factory(requireActivity().application)
    }

    private var positiveButton: Button? = null

    // Creates the dialog with password change form
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogChangePasswordBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setView(binding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener { dialogInterface ->
            positiveButton = (dialogInterface as androidx.appcompat.app.AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            setupPositiveButton(positiveButton!!)
            observeViewModel()
        }

        return dialog
    }

    // Sets up the save button click handler
    private fun setupPositiveButton(button: Button) {
        button.setOnClickListener {
            val currentPassword = binding.currentPasswordInput.text.toString()
            val newPassword = binding.newPasswordInput.text.toString()
            val confirmNewPassword = binding.confirmNewPasswordInput.text.toString()

            viewModel.changePassword(
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmNewPassword = confirmNewPassword
            )
        }
    }

    // Observes ViewModel state changes
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        ChangePasswordUiState.Initial -> {
                            hideError()
                            enableInputs(true)
                        }
                        ChangePasswordUiState.Loading -> {
                            hideError()
                            enableInputs(false)
                        }
                        ChangePasswordUiState.Success -> {
                            hideError()
                            dismiss()
                            showSuccessMessage()
                        }
                        is ChangePasswordUiState.Error -> {
                            showError(state.message)
                            enableInputs(true)
                        }
                    }
                }
            }
        }
    }

    // Enables or disables input fields and button
    private fun enableInputs(enabled: Boolean) {
        binding.currentPasswordInput.isEnabled = enabled
        binding.newPasswordInput.isEnabled = enabled
        binding.confirmNewPasswordInput.isEnabled = enabled
        positiveButton?.isEnabled = enabled
    }

    // Shows error message to the user
    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.isVisible = true
        positiveButton?.isEnabled = true
    }

    // Hides error message
    private fun hideError() {
        binding.errorText.isVisible = false
        positiveButton?.isEnabled = true
    }

    // Notifies parent about successful password change
    private fun showSuccessMessage() {
        (parentFragment as? PasswordChangeListener)?.onPasswordChanged()
            ?: (activity as? PasswordChangeListener)?.onPasswordChanged()
    }

    // Cleans up view binding when dialog is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface PasswordChangeListener {
        fun onPasswordChanged()
    }

    companion object {
        const val TAG = "ChangePasswordDialog"

        // Creates a new instance of ChangePasswordDialog
        fun newInstance() = ChangePasswordDialog()
    }
}
