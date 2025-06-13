package com.example.rentease.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.model.Result
import com.example.rentease.data.model.User
import com.example.rentease.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//Handles loading and managing landlord accounts for admin users.
class LandlordManagementViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val userRepository = RepositoryProvider.provideUserRepository(application)

    private val _uiState = MutableStateFlow<LandlordManagementUiState>(LandlordManagementUiState.Loading)
    val uiState: StateFlow<LandlordManagementUiState> = _uiState

    //Load all landlords from the repository.
    fun loadLandlords() {
        viewModelScope.launch {
            try {
                _uiState.value = LandlordManagementUiState.Loading
                when (val result = userRepository.getLandlords()) {
                    is Result.Success -> {
                        _uiState.value = LandlordManagementUiState.Success(result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = LandlordManagementUiState.Error(result.errorMessage ?: "Failed to load landlords")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = LandlordManagementUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    //Delete a landlord
    fun deleteLandlord(landlordId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = LandlordManagementUiState.Loading

                when (val result = userRepository.deleteLandlord(landlordId)) {
                    is com.example.rentease.data.model.Result.Success -> {
                        // Reload the landlords list after successful deletion
                        loadLandlords()
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = LandlordManagementUiState.Error(result.errorMessage ?: "Failed to delete landlord")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = LandlordManagementUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    //Factory for creating LandlordManagementViewModel instances.
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LandlordManagementViewModel::class.java)) {
                return LandlordManagementViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

//Updated to use User model for consistency.
sealed class LandlordManagementUiState {
    data object Loading : LandlordManagementUiState()
    data class Success(val landlords: List<User>) : LandlordManagementUiState()
    data class Error(val message: String) : LandlordManagementUiState()
}
