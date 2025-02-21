package com.example.rentease.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.model.User
import com.example.rentease.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                repository.getCurrentUser()
                    .onSuccess { user ->
                        _uiState.value = ProfileUiState.Success(user)
                    }
                    .onFailure { error ->
                        _uiState.value = ProfileUiState.Error(error.message ?: "Failed to load profile")
                    }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updateProfile(
        fullName: String,
        email: String,
        phone: String
    ) {
        if (!validateInput(fullName, email, phone)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                // TODO: Implement update profile API call
                // For now, we'll just show the loading state and then revert back
                loadProfile()
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun validateInput(
        fullName: String,
        email: String,
        phone: String
    ): Boolean {
        if (fullName.isBlank() || email.isBlank() || phone.isBlank()) {
            _uiState.value = ProfileUiState.Error("All fields are required")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = ProfileUiState.Error("Please enter a valid email address")
            return false
        }

        return true
    }

    fun logout() {
        repository.logout()
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
