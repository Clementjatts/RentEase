package com.example.rentease.ui.request

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import com.example.rentease.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the RequestFormFragment.
 */
class RequestFormViewModel(
    application: Application,
    private val propertyId: Int,
    private val landlordId: Int
) : AndroidViewModel(application) {
    
    private val propertyRepository = PropertyRepository(ApiClient.api, application)
    private val requestRepository = RequestRepository(ApiClient.api, application)
    
    private val _uiState = MutableStateFlow<RequestFormUiState>(RequestFormUiState.Loading)
    val uiState: StateFlow<RequestFormUiState> = _uiState
    
    /**
     * Load property details for the given property ID.
     */
    fun loadPropertyDetails() {
        viewModelScope.launch {
            try {
                _uiState.value = RequestFormUiState.Loading
                
                val propertyResult = propertyRepository.getPropertyById(propertyId)
                
                if (propertyResult.isSuccess) {
                    val property = (propertyResult as com.example.rentease.data.model.Result.Success<Property>).data
                    _uiState.value = RequestFormUiState.PropertyLoaded(property)
                } else {
                    val errorMsg = (propertyResult as? com.example.rentease.data.model.Result.Error)?.errorMessage ?: "Property not found"
                    _uiState.value = RequestFormUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = RequestFormUiState.Error(e.message ?: "Failed to load property details")
            }
        }
    }
    
    /**
     * Submit a contact request for the property.
     * This forwards the request directly to the landlord's email.
     */
    fun submitRequest(name: String, email: String, phone: String, message: String) {
        viewModelScope.launch {
            try {
                _uiState.value = RequestFormUiState.Loading
                
                // Get property details for the email subject
                val propertyResult = propertyRepository.getPropertyById(propertyId)
                val propertyTitle = if (propertyResult.isSuccess) {
                    val property = (propertyResult as com.example.rentease.data.model.Result.Success<Property>).data
                    property.title
                } else {
                    "Property #$propertyId"
                }
                
                // Format the message to include all user details
                val formattedMessage = """
                    |Name: $name
                    |Email: $email
                    |Phone: $phone
                    |
                    |Message:
                    |$message
                """.trimMargin()
                
                // Submit the inquiry via email
                val result = requestRepository.submitPropertyInquiry(
                    userId = "user123", // This would come from auth in a real app
                    propertyId = propertyId,
                    landlordId = landlordId.toString(),
                    name = name,
                    email = email,
                    subject = "Inquiry about $propertyTitle",
                    message = formattedMessage
                )
                
                if (result is com.example.rentease.data.model.Result.Success) {
                    _uiState.value = RequestFormUiState.Success
                } else {
                    val errorMsg = (result as? com.example.rentease.data.model.Result.Error)?.errorMessage ?: "Failed to submit request"
                    _uiState.value = RequestFormUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = RequestFormUiState.Error(e.message ?: "Failed to submit request")
            }
        }
    }
    
    /**
     * Factory for creating RequestFormViewModel instances.
     */
    class Factory(
        private val application: Application,
        private val propertyId: Int,
        private val landlordId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RequestFormViewModel::class.java)) {
                return RequestFormViewModel(application, propertyId, landlordId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * UI state for the request form screen.
 */
sealed class RequestFormUiState {
    object Loading : RequestFormUiState()
    data class PropertyLoaded(val property: Property) : RequestFormUiState()
    object Success : RequestFormUiState()
    data class Error(val message: String) : RequestFormUiState()
}
