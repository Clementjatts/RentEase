package com.example.rentease.ui.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.UserType
import com.example.rentease.data.model.User
import com.example.rentease.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Initial)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(
        username: String,
        password: String,
        confirmPassword: String,
        email: String,
        fullName: String,
        phone: String,
        userType: UserType
    ) {
        if (!validateInput(username, password, confirmPassword, email, fullName, phone)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            try {
                repository.register(
                    username = username,
                    password = password,
                    email = email,
                    fullName = fullName,
                    phone = phone,
                    userType = userType
                ).onSuccess { user ->
                    _uiState.value = RegisterUiState.Success(user)
                }.onFailure { error ->
                    _uiState.value = RegisterUiState.Error(error.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun validateInput(
        username: String,
        password: String,
        confirmPassword: String,
        email: String,
        fullName: String,
        phone: String
    ): Boolean {
        if (username.isBlank() || password.isBlank() || confirmPassword.isBlank() ||
            email.isBlank() || fullName.isBlank() || phone.isBlank()
        ) {
            _uiState.value = RegisterUiState.Error("All fields are required")
            return false
        }

        if (username.length < 3) {
            _uiState.value = RegisterUiState.Error("Username must be at least 3 characters long")
            return false
        }

        if (password.length < 6) {
            _uiState.value = RegisterUiState.Error("Password must be at least 6 characters long")
            return false
        }

        if (password != confirmPassword) {
            _uiState.value = RegisterUiState.Error("Passwords do not match")
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = RegisterUiState.Error("Please enter a valid email address")
            return false
        }

        if (!Patterns.PHONE.matcher(phone).matches()) {
            _uiState.value = RegisterUiState.Error("Please enter a valid phone number")
            return false
        }

        return true
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class RegisterUiState {
    data object Initial : RegisterUiState()
    data object Loading : RegisterUiState()
    data class Success(val user: User) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
