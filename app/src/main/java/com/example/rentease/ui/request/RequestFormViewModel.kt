package com.example.rentease.ui.request

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.model.Property
import com.example.rentease.data.model.Request
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
                
                val property = propertyRepository.getPropertyById(propertyId)
                
                if (property != null) {
                    _uiState.value = RequestFormUiState.PropertyLoaded(property)
                } else {
                    _uiState.value = RequestFormUiState.Error("Property not found")
                }
            } catch (e: Exception) {
                _uiState.value = RequestFormUiState.Error(e.message ?: "Failed to load property details")
            }
        }
    }
    
    /**
     * Submit a contact request for the property.
     */
    fun submitRequest(name: String, email: String, phone: String, message: String) {
        viewModelScope.launch {
            try {
                _uiState.value = RequestFormUiState.Loading
                
                val request = Request(
                    id = 0, // Will be assigned by the server
                    propertyId = propertyId,
                    landlordId = landlordId,
                    name = name,
                    email = email,
                    phone = phone,
                    message = message,
                    status = "pending",
                    createdAt = System.currentTimeMillis()
                )
                
                val result = requestRepository.submitRequest(request)
                
                if (result.isSuccess) {
                    _uiState.value = RequestFormUiState.Success
                } else {
                    _uiState.value = RequestFormUiState.Error(result.errorMessage ?: "Failed to submit request")
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
