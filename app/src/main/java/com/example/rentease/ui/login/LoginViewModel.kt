package com.example.rentease.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.AuthManager
import com.example.rentease.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel that handles the business logic for the login screen
class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepository = RepositoryProvider.provideAuthRepository(application)
    private val authManager = AuthManager.getInstance(application)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState

    // Attempts to log in with the provided credentials
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Email and password cannot be empty")
            return
        }

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {
                // Try to login with the email as the username
                val result = authRepository.login(email, password)
                if (result.isSuccess) {
                    // The user is already logged in by the repository
                    _uiState.value = LoginUiState.Success
                } else {
                    val errorMessage = when (result) {
                        is com.example.rentease.data.model.Result.Error -> result.errorMessage ?: "Login failed"
                        else -> "Login failed"
                    }
                    android.util.Log.e("LoginViewModel", "Login failed: $errorMessage")
                    _uiState.value = LoginUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Login exception", e)
                _uiState.value = LoginUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    // Resets the UI state to initial
    fun resetState() {
        _uiState.value = LoginUiState.Initial
    }

    // Returns the current user type from AuthManager
    fun getUserType() = authManager.userType

    // Factory for creating LoginViewModel instances
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// Represents the UI state for the login screen
sealed class LoginUiState {
    data object Initial : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
