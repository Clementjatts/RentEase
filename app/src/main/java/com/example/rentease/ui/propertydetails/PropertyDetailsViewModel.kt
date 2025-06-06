package com.example.rentease.ui.propertydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PropertyDetailsViewModel(
    private val propertyId: Int,
    private val repository: PropertyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PropertyDetailsUiState>(PropertyDetailsUiState.Loading)
    val uiState: StateFlow<PropertyDetailsUiState> = _uiState

    init {
        loadProperty()
    }

    private fun loadProperty() {
        viewModelScope.launch {
            // Check if property ID is valid
            if (propertyId <= 0) {
                android.util.Log.e("PropertyDetailsViewModel", "Invalid property ID: $propertyId")
                _uiState.value = PropertyDetailsUiState.Error("Invalid property ID. Please try again.")
                return@launch
            }

            _uiState.value = PropertyDetailsUiState.Loading
            try {
                when (val result = repository.getProperty(propertyId)) {
                    is com.example.rentease.data.model.Result.Success -> {
                        _uiState.value = PropertyDetailsUiState.Success(result.data)
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        android.util.Log.e("PropertyDetailsViewModel", "Error loading property: ${result.errorMessage}")
                        _uiState.value = PropertyDetailsUiState.Error(result.errorMessage ?: "Failed to load property")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PropertyDetailsViewModel", "Exception loading property", e)
                _uiState.value = PropertyDetailsUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

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

sealed class PropertyDetailsUiState {
    data object Loading : PropertyDetailsUiState()
    data class Success(val property: Property) : PropertyDetailsUiState()
    data class Error(val message: String) : PropertyDetailsUiState()
}
