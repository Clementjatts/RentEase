package com.example.rentease.ui.contact

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.repository.PropertyRepository
import com.example.rentease.data.repository.RequestRepository
import com.example.rentease.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ContactFormViewModel handles the business logic for the contact form screen.
 */
class ContactFormViewModel(
    application: Application,
    private val propertyId: Int
) : AndroidViewModel(application) {
    
    private val requestRepository = RepositoryProvider.provideRequestRepository(application)
    private val propertyRepository = RepositoryProvider.providePropertyRepository(application)
    private val authManager = AuthManager.getInstance(application)
    
    private val _uiState = MutableStateFlow<ContactFormUiState>(ContactFormUiState.Initial)
    val uiState: StateFlow<ContactFormUiState> = _uiState
    
    /**
     * Submit the contact form to the repository.
     */
    fun submitContactForm(
        name: String,
        email: String,
        subject: String,
        message: String
    ) {
        _uiState.value = ContactFormUiState.Loading
        
        viewModelScope.launch {
            try {
                val userId = authManager.getUserId()
                
                // If propertyId is valid, this is a property inquiry
                val result = if (propertyId != -1) {
                    // Get property details to include landlord information
                    val propertyResult = propertyRepository.getProperty(propertyId)
                    
                    if (!propertyResult.isSuccess) {
                        _uiState.value = ContactFormUiState.Error(
                            propertyResult.errorMessage ?: "Failed to get property details"
                        )
                        return@launch
                    }
                    
                    val property = propertyResult.property
                    
                    // Submit property inquiry
                    requestRepository.submitPropertyInquiry(
                        userId = userId,
                        propertyId = propertyId,
                        landlordId = property.landlordId,
                        name = name,
                        email = email,
                        subject = subject,
                        message = message
                    )
                } else {
                    // Submit general contact form
                    requestRepository.submitContactForm(
                        userId = userId,
                        name = name,
                        email = email,
                        subject = subject,
                        message = message
                    )
                }
                
                if (result.isSuccess) {
                    _uiState.value = ContactFormUiState.Success
                } else {
                    _uiState.value = ContactFormUiState.Error(result.errorMessage ?: "Failed to submit form")
                }
            } catch (e: Exception) {
                _uiState.value = ContactFormUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
    
    /**
     * Factory for creating ContactFormViewModel instances.
     */
    class Factory(
        private val application: Application,
        private val propertyId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactFormViewModel::class.java)) {
                return ContactFormViewModel(application, propertyId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Represents the UI state for the contact form screen.
 */
sealed class ContactFormUiState {
    object Initial : ContactFormUiState()
    object Loading : ContactFormUiState()
    object Success : ContactFormUiState()
    data class Error(val message: String) : ContactFormUiState()
}
