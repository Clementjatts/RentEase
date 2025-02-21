package com.example.rentease.ui.contact

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.model.UserRequest
import com.example.rentease.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContactFormViewModel(
    private val propertyId: Int,
    private val repository: PropertyRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactFormUiState>(ContactFormUiState.Initial)
    val uiState: StateFlow<ContactFormUiState> = _uiState

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

                // TODO: Create UserRequestRepository and implement this
                // repository.createRequest(request)
                _uiState.value = ContactFormUiState.Success
            } catch (e: Exception) {
                _uiState.value = ContactFormUiState.Error(e.message ?: "Failed to submit request")
            }
        }
    }

    private fun validateInput(name: String, email: String, message: String): Boolean {
        if (name.isBlank() || email.isBlank() || message.isBlank()) {
            _uiState.value = ContactFormUiState.Error("All fields are required")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = ContactFormUiState.Error("Please enter a valid email address")
            return false
        }

        return true
    }

    class Factory(
        private val propertyId: Int,
        private val repository: PropertyRepository,
        private val savedStateHandle: SavedStateHandle
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactFormViewModel::class.java)) {
                return ContactFormViewModel(propertyId, repository, savedStateHandle) as T
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
