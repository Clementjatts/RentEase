package com.example.rentease.ui.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ChangePasswordViewModel handles password change logic
class ChangePasswordViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChangePasswordUiState>(ChangePasswordUiState.Initial)
    val uiState: StateFlow<ChangePasswordUiState> = _uiState

    // Changes user password with validation
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ) {
        if (!validateInput(currentPassword, newPassword, confirmNewPassword)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = ChangePasswordUiState.Loading

            try {
                val result = repository.changePassword(currentPassword, newPassword)

                if (result is com.example.rentease.data.model.Result.Success) {
                    _uiState.value = ChangePasswordUiState.Success
                } else if (result is com.example.rentease.data.model.Result.Error) {
                    _uiState.value = ChangePasswordUiState.Error(
                        result.errorMessage ?: "Failed to change password"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ChangePasswordUiState.Error(
                    e.message ?: "An error occurred"
                )
            }
        }
    }

    // Validates password change input
    private fun validateInput(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Boolean {
        if (currentPassword.isBlank() || newPassword.isBlank() || confirmNewPassword.isBlank()) {
            _uiState.value = ChangePasswordUiState.Error("All fields are required")
            return false
        }

        if (newPassword.length < 6) {
            _uiState.value = ChangePasswordUiState.Error("New password must be at least 6 characters long")
            return false
        }

        if (newPassword != confirmNewPassword) {
            _uiState.value = ChangePasswordUiState.Error("New passwords do not match")
            return false
        }

        if (currentPassword == newPassword) {
            _uiState.value = ChangePasswordUiState.Error("New password must be different from current password")
            return false
        }

        return true
    }

    // Factory for creating ChangePasswordViewModel instances
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        // Creates ViewModel instances for password change dialog
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChangePasswordViewModel::class.java)) {
                val authManager = AuthManager.getInstance(application)
                val repository = AuthRepository(ApiClient.getApi(application), authManager)
                return ChangePasswordViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// Represents the UI state for password change dialog
sealed class ChangePasswordUiState {
    data object Initial : ChangePasswordUiState()
    data object Loading : ChangePasswordUiState()
    data object Success : ChangePasswordUiState()
    data class Error(val message: String) : ChangePasswordUiState()
}
