package com.example.rentease.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.AuthManager
import com.example.rentease.auth.UserType
import com.example.rentease.data.model.User
import com.example.rentease.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ProfileViewModel handles the business logic for the profile screen.
 */
class ProfileViewModel(
    application: Application,
    private val landlordId: Int? = null
) : AndroidViewModel(application) {

    private val userRepository = RepositoryProvider.provideUserRepository(application)
    private val authManager = AuthManager.getInstance(application)

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Initial)
    val uiState: StateFlow<ProfileUiState> = _uiState

    /**
     * Load the user data from the repository.
     */
    fun loadUserData() {
        _uiState.value = ProfileUiState.Loading

        viewModelScope.launch {
            try {
                val result = userRepository.getUserProfile(landlordId)

                if (result is com.example.rentease.data.model.Result.Success) {
                    val user = result.data

                    val joinDateFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(System.currentTimeMillis()))

                    val formattedDate = if (user.createdAt.isNullOrEmpty()) {
                        joinDateFormatted
                    } else {
                        user.createdAt
                    }

                    _uiState.value = ProfileUiState.UserData(
                        username = user.username,
                        fullName = user.fullName ?: "",
                        email = user.email ?: "",
                        phone = user.phone ?: "",
                        userType = UserType.fromString(user.userType),
                        joinDate = formattedDate
                    )
                } else if (result is com.example.rentease.data.model.Result.Error) {
                    android.util.Log.e("ProfileViewModel", "Failed to load user data: ${result.errorMessage}")
                    _uiState.value = ProfileUiState.Error(result.errorMessage ?: "Failed to load user data")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * Reload user data without showing loading state.
     * This is used after successful operations to refresh the data.
     */
    private fun reloadUserDataSilently() {
        viewModelScope.launch {
            try {

                val result = userRepository.getUserProfile(landlordId)

                if (result is com.example.rentease.data.model.Result.Success) {
                    val user = result.data
                    val joinDateFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(System.currentTimeMillis()))

                    val formattedDate = if (user.createdAt.isNullOrEmpty()) {
                        joinDateFormatted
                    } else {
                        user.createdAt
                    }

                    _uiState.value = ProfileUiState.UserData(
                        username = user.username,
                        fullName = user.fullName ?: "",
                        email = user.email ?: "",
                        phone = user.phone ?: "",
                        userType = UserType.fromString(user.userType),
                        joinDate = formattedDate
                    )
                }

            } catch (_: Exception) {

            }
        }
    }

    /**
     * Update the user profile.
     */
    fun updateProfile(
        fullName: String,
        email: String,
        phone: String
    ) {
        if (fullName.isBlank() || email.isBlank() || phone.isBlank()) {
            _uiState.value = ProfileUiState.Error("All fields are required")
            return
        }

        _uiState.value = ProfileUiState.Loading

        viewModelScope.launch {
            try {
                // Determine which user ID to update
                val targetUserId = if (landlordId != null && landlordId > 0) {
                    // If we're editing a specific landlord profile, use the landlord ID
                    landlordId
                } else {
                    // Otherwise, update the current user's profile
                    authManager.getUserId().toIntOrNull() ?: 0
                }

                if (targetUserId <= 0) {
                    _uiState.value = ProfileUiState.Error("Invalid user ID")
                    return@launch
                }

                val result = userRepository.updateUserProfile(
                    User(
                        id = targetUserId,
                        username = "", // Keep existing username
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        userType = "", // Keep existing userType
                        createdAt = "" // Keep existing createdAt
                    ),
                    landlordId = landlordId // Pass landlordId to repository for proper update handling
                )

                if (result is com.example.rentease.data.model.Result.Success) {
                    _uiState.value = ProfileUiState.Success("Profile updated successfully")
                    // Reload user data to reflect changes without showing loading state
                    reloadUserDataSilently()
                } else if (result is com.example.rentease.data.model.Result.Error) {
                    _uiState.value = ProfileUiState.Error(result.errorMessage ?: "Failed to update profile")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "An error occurred")
            }
        }
    }



    /**
     * Factory for creating ProfileViewModel instances.
     */
    class Factory(private val application: Application, private val landlordId: Int? = null) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(application, landlordId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Represents the UI state for the profile screen.
 */
sealed class ProfileUiState {
    data object Initial : ProfileUiState()
    data object Loading : ProfileUiState()
    data class Success(val message: String) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    data class UserData(
        val username: String,
        val fullName: String,
        val email: String,
        val phone: String,
        val userType: UserType,
        val joinDate: String
    ) : ProfileUiState()
}
