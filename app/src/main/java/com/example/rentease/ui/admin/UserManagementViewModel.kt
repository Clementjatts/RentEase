package com.example.rentease.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.model.Landlord
import com.example.rentease.data.repository.UserRepository
import com.example.rentease.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * UserManagementViewModel handles the business logic for the user management screen.
 */
class UserManagementViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val userRepository = RepositoryProvider.provideUserRepository(application)
    
    private val _uiState = MutableStateFlow<UserManagementUiState>(UserManagementUiState.Loading)
    val uiState: StateFlow<UserManagementUiState> = _uiState
    
    /**
     * Load the landlords from the repository.
     */
    fun loadLandlords() {
        _uiState.value = UserManagementUiState.Loading
        
        viewModelScope.launch {
            try {
                val result = userRepository.getLandlords()
                
                if (result.isSuccess) {
                    _uiState.value = UserManagementUiState.Success(result.landlords)
                } else {
                    _uiState.value = UserManagementUiState.Error(result.errorMessage ?: "Failed to load landlords")
                }
            } catch (e: Exception) {
                _uiState.value = UserManagementUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
    
    /**
     * Approve a landlord.
     */
    fun approveLandlord(landlordId: Int) {
        viewModelScope.launch {
            try {
                val result = userRepository.approveLandlord(landlordId)
                
                if (result.isSuccess) {
                    // Reload landlords after approval
                    loadLandlords()
                } else {
                    _uiState.value = UserManagementUiState.Error(result.errorMessage ?: "Failed to approve landlord")
                }
            } catch (e: Exception) {
                _uiState.value = UserManagementUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
    
    /**
     * Reject a landlord.
     */
    fun rejectLandlord(landlordId: Int) {
        viewModelScope.launch {
            try {
                val result = userRepository.rejectLandlord(landlordId)
                
                if (result.isSuccess) {
                    // Reload landlords after rejection
                    loadLandlords()
                } else {
                    _uiState.value = UserManagementUiState.Error(result.errorMessage ?: "Failed to reject landlord")
                }
            } catch (e: Exception) {
                _uiState.value = UserManagementUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
    
    /**
     * Factory for creating UserManagementViewModel instances.
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserManagementViewModel::class.java)) {
                return UserManagementViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Represents the UI state for the user management screen.
 */
sealed class UserManagementUiState {
    object Loading : UserManagementUiState()
    data class Success(val landlords: List<Landlord>) : UserManagementUiState()
    data class Error(val message: String) : UserManagementUiState()
}
