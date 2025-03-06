package com.example.rentease.ui.propertydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PropertyDetailsViewModel(
    private val repository: PropertyRepository,
    private val propertyId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<PropertyDetailsUiState>(PropertyDetailsUiState.Loading)
    val uiState: StateFlow<PropertyDetailsUiState> = _uiState

    init {
        loadProperty()
    }

    fun loadProperty() {
        viewModelScope.launch {
            _uiState.value = PropertyDetailsUiState.Loading

            try {
                val result = repository.getProperty(propertyId)
                when (result) {
                    is com.example.rentease.data.model.Result.Success -> {
                        val property = result.data
                        _uiState.value = PropertyDetailsUiState.Success(property)
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyDetailsUiState.Error("Failed to load property: ${result.errorMessage}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PropertyDetailsUiState.Error("An error occurred: ${e.message}")
            }
        }
    }

    fun refresh() {
        loadProperty()
    }

    fun contactLandlord(property: Property) {
        // Implement contact functionality
        viewModelScope.launch {
            try {
                // Check if the property has landlord information
                if (property.landlordId <= 0) {
                    _uiState.value = PropertyDetailsUiState.Error("Landlord information not available")
                    return@launch
                }
                
                // Get landlord contact information from repository if needed
                // This could involve fetching additional details or checking if user is authorized
                
                // Update UI state to indicate contact is in progress or ready
                // For example, this could navigate to contact form or show dialog
                (_uiState.value as? PropertyDetailsUiState.Success)?.let { currentState ->
                    _uiState.value = PropertyDetailsUiState.ContactReady(
                        property = currentState.property,
                        landlordContact = LandlordContact(
                            id = property.landlordId,
                            email = "contact@example.com", // Placeholder email
                            phone = "555-123-4567" // Placeholder phone
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PropertyDetailsUiState.Error("Failed to contact landlord: ${e.message}")
            }
        }
    }
}

sealed class PropertyDetailsUiState {
    data object Loading : PropertyDetailsUiState()
    data class Success(val property: Property) : PropertyDetailsUiState()
    data class Error(val message: String) : PropertyDetailsUiState()
    data class ContactReady(
        val property: Property,
        val landlordContact: LandlordContact
    ) : PropertyDetailsUiState()
}

data class LandlordContact(
    val id: Int,
    val email: String,
    val phone: String
)
