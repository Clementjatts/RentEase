package com.example.rentease.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.UserType
import com.example.rentease.data.model.User
import com.example.rentease.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String, userType: UserType) {
        if (!validateInput(username, password)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                repository.login(username, password, userType)
                    .onSuccess { user ->
                        _uiState.value = LoginUiState.Success(user)
                    }
                    .onFailure { error ->
                        _uiState.value = LoginUiState.Error(error.message ?: "Login failed")
                    }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Username and password are required")
            return false
        }
        return true
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class LoginUiState {
    data object Initial : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
