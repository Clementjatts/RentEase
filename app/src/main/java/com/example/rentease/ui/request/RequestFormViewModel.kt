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
    private val propertyId: Int
) : AndroidViewModel(application) {
    
    private val propertyRepository = PropertyRepository(ApiClient.getApi(application), application)
    private val requestRepository = RequestRepository()
    
    private val _uiState = MutableStateFlow<RequestFormUiState>(RequestFormUiState.Loading)
    val uiState: StateFlow<RequestFormUiState> = _uiState
    
    // Load property details
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
    
    // Submit contact request to landlord via email
    fun submitRequest(name: String, email: String, phone: String, message: String) {
        viewModelScope.launch {
            try {
                _uiState.value = RequestFormUiState.Loading

                // Get current property data from the UI state
                val currentState = _uiState.value
                val property = if (currentState is RequestFormUiState.PropertyLoaded) {
                    currentState.property
                } else {
                    // If property not loaded, try to load it first
                    val propertyResult = propertyRepository.getPropertyById(propertyId)
                    if (propertyResult.isSuccess) {
                        (propertyResult as com.example.rentease.data.model.Result.Success<Property>).data
                    } else {
                        _uiState.value = RequestFormUiState.Error("Property information not available")
                        return@launch
                    }
                }

                // Validate that we have landlord contact information
                if (property.landlordContact.isNullOrBlank()) {
                    _uiState.value = RequestFormUiState.Error("Landlord contact information not available")
                    return@launch
                }

                // Submit the inquiry via email
                val result = requestRepository.submitPropertyInquiry(
                    propertyTitle = property.title,
                    landlordContact = property.landlordContact!!,
                    propertyId = propertyId,
                    name = name,
                    email = email,
                    phone = phone.takeIf { it.isNotBlank() },
                    message = message
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
    
    // Factory for creating RequestFormViewModel instances
    class Factory(
        private val application: Application,
        private val propertyId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RequestFormViewModel::class.java)) {
                return RequestFormViewModel(application, propertyId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// UI state for the request form screen
sealed class RequestFormUiState {
    data object Loading : RequestFormUiState()
    data class PropertyLoaded(val property: Property) : RequestFormUiState()
    data object Success : RequestFormUiState()
    data class Error(val message: String) : RequestFormUiState()
}
