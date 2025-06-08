package com.example.rentease.ui.landlord

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.AuthManager
import com.example.rentease.auth.UserType
import com.example.rentease.data.model.Property
import com.example.rentease.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * PropertyManagementViewModel handles the business logic for the property management screen.
 */
class PropertyManagementViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val propertyRepository = RepositoryProvider.providePropertyRepository(application)
    private val authManager = AuthManager.getInstance(application)

    private val _uiState = MutableStateFlow<PropertyManagementUiState>(PropertyManagementUiState.Loading)
    val uiState: StateFlow<PropertyManagementUiState> = _uiState

    /**
     * Load the landlord's properties from the repository.
     */
    fun loadProperties() {
        _uiState.value = PropertyManagementUiState.Loading

        viewModelScope.launch {
            try {
                // Check if the user is an admin or a landlord
                val userType = authManager.userType
                val userId = authManager.getUserId()

                val result = if (userType == UserType.ADMIN) {
                    // Admin sees all properties
                    propertyRepository.getProperties()
                } else {
                    // Landlord sees only their properties
                    // Since landlord_id = user_id in the backend, we can use userId directly
                    val userIdInt = userId.toIntOrNull()
                    if (userIdInt != null && userIdInt > 0) {
                        propertyRepository.getLandlordProperties(userIdInt.toString())
                    } else {
                        com.example.rentease.data.model.Result.Error("Invalid user ID")
                    }
                }

                when (result) {
                    is com.example.rentease.data.model.Result.Success -> {
                        _uiState.value = PropertyManagementUiState.Success(result.data)
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyManagementUiState.Error(result.errorMessage ?: "Failed to load properties")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PropertyManagementVM", "Exception loading properties", e)
                _uiState.value = PropertyManagementUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * Delete a property from the repository.
     */
    fun deleteProperty(propertyId: Int) {
        viewModelScope.launch {
            try {
                when (val result = propertyRepository.deleteProperty(propertyId)) {
                    is com.example.rentease.data.model.Result.Success -> {
                        // Reload properties after deletion
                        loadProperties()
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyManagementUiState.Error(result.errorMessage ?: "Failed to delete property")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PropertyManagementUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * Factory for creating PropertyManagementViewModel instances.
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PropertyManagementViewModel::class.java)) {
                return PropertyManagementViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Represents the UI state for the property management screen.
 */
sealed class PropertyManagementUiState {
    data object Loading : PropertyManagementUiState()
    data class Success(val properties: List<Property>) : PropertyManagementUiState()
    data class Error(val message: String) : PropertyManagementUiState()
}
