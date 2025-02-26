package com.example.rentease.ui.profile

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.rentease.R
import com.example.rentease.databinding.DialogChangePasswordBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ChangePasswordDialog : DialogFragment() {
    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChangePasswordViewModel by viewModels {
        ChangePasswordViewModel.Factory(requireActivity().application)
    }

    private var positiveButton: Button? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogChangePasswordBinding.inflate(LayoutInflater.from(context))

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setView(binding.root)
            .setPositiveButton("Change Password", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener { dialogInterface ->
            positiveButton = (dialogInterface as androidx.appcompat.app.AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            setupPositiveButton(positiveButton!!)
            observeViewModel()
        }

        return dialog
    }

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

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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

    private fun enableInputs(enabled: Boolean) {
        binding.currentPasswordInput.isEnabled = enabled
        binding.newPasswordInput.isEnabled = enabled
        binding.confirmNewPasswordInput.isEnabled = enabled
        positiveButton?.isEnabled = enabled
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.isVisible = true
        positiveButton?.isEnabled = true
    }

    private fun hideError() {
        binding.errorText.isVisible = false
        positiveButton?.isEnabled = true
    }

    private fun showSuccessMessage() {
        (parentFragment as? PasswordChangeListener)?.onPasswordChanged()
            ?: (activity as? PasswordChangeListener)?.onPasswordChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface PasswordChangeListener {
        fun onPasswordChanged()
    }

    companion object {
        const val TAG = "ChangePasswordDialog"

        fun newInstance() = ChangePasswordDialog()
    }
}
