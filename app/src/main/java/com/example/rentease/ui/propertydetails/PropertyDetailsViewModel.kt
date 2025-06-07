package com.example.rentease.ui.propertydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the PropertyDetailsFragment.
 * Handles loading and managing property details.
 */
class PropertyDetailsViewModel(
    private val propertyId: Int,
    private val repository: PropertyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PropertyDetailsUiState>(PropertyDetailsUiState.Loading)
    val uiState: StateFlow<PropertyDetailsUiState> = _uiState

    private var currentProperty: Property? = null

    init {
        loadProperty()
    }

    private fun loadProperty() {
        viewModelScope.launch {
            try {
                // Check if property ID is valid
                if (propertyId <= 0) {
                    _uiState.value = PropertyDetailsUiState.Error("Invalid property ID. Please try again.")
                    return@launch
                }

                _uiState.value = PropertyDetailsUiState.Loading
                when (val result = repository.getPropertyById(propertyId)) {
                    is com.example.rentease.data.model.Result.Success -> {
                        currentProperty = result.data
                        _uiState.value = PropertyDetailsUiState.Success(result.data)
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyDetailsUiState.Error(result.errorMessage ?: "Failed to load property")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PropertyDetailsUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * Get the landlord ID for the current property.
     * This is used when navigating to the request form.
     */
    fun getLandlordId(): Int {
        return currentProperty?.landlordId ?: -1
    }

    /**
     * Factory for creating PropertyDetailsViewModel instances.
     */
    class Factory(
        private val propertyId: Int,
        private val repository: PropertyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PropertyDetailsViewModel::class.java)) {
                return PropertyDetailsViewModel(propertyId, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Represents the UI state for the property details screen.
 */
sealed class PropertyDetailsUiState {
    data object Loading : PropertyDetailsUiState()
    data class Success(val property: Property) : PropertyDetailsUiState()
    data class Error(val message: String) : PropertyDetailsUiState()
}

