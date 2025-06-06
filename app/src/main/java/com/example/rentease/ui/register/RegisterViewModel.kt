package com.example.rentease.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.UserType
import com.example.rentease.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * RegisterViewModel handles the business logic for the registration screen.
 */
class RegisterViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepository = RepositoryProvider.provideAuthRepository(application)

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Initial)
    val uiState: StateFlow<RegisterUiState> = _uiState

    /**
     * Attempt to register a new user with the provided information.
     */
    fun register(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        userType: UserType,
        isFromAdmin: Boolean = false
    ) {
        // Validate input
        val validationError = validateInput(
            username, fullName, email, phone, password, confirmPassword
        )

        if (validationError != null) {
            _uiState.value = RegisterUiState.Error(validationError)
            return
        }

        _uiState.value = RegisterUiState.Loading

        viewModelScope.launch {
            try {
                val result = if (isFromAdmin) {
                    // Admin creating a user - preserve admin session
                    authRepository.registerUserAsAdmin(
                        username = username,
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        password = password,
                        userType = userType
                    )
                } else {
                    // Regular user registration - login as new user
                    authRepository.register(
                        username = username,
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        password = password,
                        userType = userType
                    )
                }

                if (result.isSuccess) {
                    _uiState.value = RegisterUiState.Success
                } else {
                    val errorMsg = (result as? com.example.rentease.data.model.Result.Error)?.errorMessage ?: "Registration failed"
                    _uiState.value = RegisterUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * Validate the user input.
     *
     * @return An error message if validation fails, null otherwise.
     */
    private fun validateInput(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): String? {
        if (username.isBlank() || fullName.isBlank() || email.isBlank() ||
            phone.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            return "All fields are required"
        }

        if (password != confirmPassword) {
            return "Passwords do not match"
        }

        if (password.length < 6) {
            return "Password must be at least 6 characters"
        }

        if (!email.contains("@") || !email.contains(".")) {
            return "Invalid email address"
        }

        return null
    }

    /**
     * Reset the UI state to initial.
     */
    fun resetState() {
        _uiState.value = RegisterUiState.Initial
    }

    /**
     * Factory for creating RegisterViewModel instances.
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Represents the UI state for the registration screen.
 */
sealed class RegisterUiState {
    data object Initial : RegisterUiState()
    data object Loading : RegisterUiState()
    data object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
