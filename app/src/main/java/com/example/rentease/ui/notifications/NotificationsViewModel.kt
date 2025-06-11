package com.example.rentease.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.model.Request
import com.example.rentease.data.repository.RequestRepository
import com.example.rentease.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel for the NotificationsFragment
class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val requestRepository = RepositoryProvider.provideRequestRepository(application)
    private val authManager = AuthManager.getInstance(application)

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState

    // Loads notifications for the current landlord
    fun loadNotifications() {
        viewModelScope.launch {
            try {
                _uiState.value = NotificationsUiState.Loading

                val userId = authManager.getUserId().toIntOrNull()
                if (userId == null) {
                    _uiState.value = NotificationsUiState.Error("User not logged in")
                    return@launch
                }

                val result = requestRepository.getRequestsForLandlord(userId)
                if (result.isSuccess) {
                    val requests = (result as com.example.rentease.data.model.Result.Success<List<Request>>).data
                    if (requests.isEmpty()) {
                        _uiState.value = NotificationsUiState.Empty
                    } else {
                        _uiState.value = NotificationsUiState.Success(requests)
                    }
                } else {
                    val errorMsg = (result as com.example.rentease.data.model.Result.Error).errorMessage ?: "Unknown error"
                    _uiState.value = NotificationsUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = NotificationsUiState.Error(e.message ?: "Failed to load notifications")
            }
        }
    }

    // Marks a notification as read
    fun markAsRead(requestId: Int) {
        viewModelScope.launch {
            try {
                val result = requestRepository.markAsRead(requestId)
                if (result.isSuccess) {
                    // Reload notifications to update the UI
                    loadNotifications()
                }
            } catch (e: Exception) {
                // Mark as read is non-critical operation
            }
        }
    }

    // Factory for creating NotificationsViewModel instances
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        // Creates ViewModel instances for the notifications screen
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
                return NotificationsViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// Represents the UI state for the notifications screen
sealed class NotificationsUiState {
    data object Loading : NotificationsUiState()
    data class Success(val requests: List<Request>) : NotificationsUiState()
    data object Empty : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}
