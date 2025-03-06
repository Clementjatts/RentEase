package com.example.rentease.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.repository.AuthRepository
import com.example.rentease.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * LoginViewModel handles the business logic for the login screen.
 */
class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val authRepository = RepositoryProvider.provideAuthRepository(application)
    private val authManager = AuthManager.getInstance(application)
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState
    
    /**
     * Attempt to log in with the provided credentials.
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Email and password cannot be empty")
            return
        }
        
        _uiState.value = LoginUiState.Loading
        
        viewModelScope.launch {
            try {
                val result = authRepository.login(email, password)
                if (result.isSuccess) {
                    // The user is already logged in by the repository
                    _uiState.value = LoginUiState.Success
                } else {
                    _uiState.value = LoginUiState.Error(
                        when (result) {
                            is com.example.rentease.data.model.Result.Error -> result.errorMessage ?: "Login failed"
                            else -> "Login failed"
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
    
    /**
     * Reset the UI state to initial.
     */
    fun resetState() {
        _uiState.value = LoginUiState.Initial
    }
    
    /**
     * Get the user type from the AuthManager.
     */
    fun getUserType() = authManager.userType
    
    /**
     * Factory for creating LoginViewModel instances.
     */
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

/**
 * Represents the UI state for the login screen.
 */
sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
