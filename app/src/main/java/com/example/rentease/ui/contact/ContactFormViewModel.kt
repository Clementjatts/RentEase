package com.example.rentease.ui.contact

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.model.UserRequest
import com.example.rentease.data.repository.UserRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.Application
import android.util.Patterns

class ContactFormViewModel(
    private val repository: UserRequestRepository,
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ContactFormUiState>(ContactFormUiState.Initial)
    val uiState: StateFlow<ContactFormUiState> = _uiState
    
    private var propertyId: Int = -1

    init {
        // Get propertyId from savedStateHandle
        propertyId = savedStateHandle.get<Int>("propertyId") ?: -1
    }

    fun submitRequest(name: String, email: String, message: String) {
        if (!validateInput(name, email, message)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = ContactFormUiState.Loading

            try {
                val request = UserRequest(
                    id = 0, // Will be assigned by the server
                    userId = email, // Using email as user ID for simplicity
                    propertyId = propertyId,
                    message = message,
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date())
                )

                // Call the repository to create the request
                val result = repository.createRequest(request)
                
                if (result.isSuccess) {
                    _uiState.value = ContactFormUiState.Success
                } else {
                    _uiState.value = ContactFormUiState.Error("Failed to submit request: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                _uiState.value = ContactFormUiState.Error("An error occurred: ${e.message}")
            }
        }
    }

    private fun validateInput(name: String, email: String, message: String): Boolean {
        if (name.isBlank() || email.isBlank() || message.isBlank()) {
            _uiState.value = ContactFormUiState.Error("All fields are required")
            return false
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = ContactFormUiState.Error("Please enter a valid email address")
            return false
        }
        
        return true
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactFormViewModel::class.java)) {
                val repository = UserRequestRepository(ApiClient.api, application)
                return ContactFormViewModel(repository, application, SavedStateHandle()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class ContactFormUiState {
    data object Initial : ContactFormUiState()
    data object Loading : ContactFormUiState()
    data object Success : ContactFormUiState()
    data class Error(val message: String) : ContactFormUiState()
}
