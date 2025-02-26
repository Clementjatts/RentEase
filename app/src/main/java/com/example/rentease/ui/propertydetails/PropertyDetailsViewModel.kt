package com.example.rentease.ui.propertydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PropertyDetailsViewModel(
    private val repository: PropertyRepository,
    private val propertyId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<PropertyDetailsUiState>(PropertyDetailsUiState.Loading)
    val uiState: StateFlow<PropertyDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProperty()
    }

    private fun loadProperty() {
        viewModelScope.launch {
            _uiState.value = PropertyDetailsUiState.Loading
            try {
                repository.getProperty(propertyId)
                    .onSuccess { property ->
                        _uiState.value = PropertyDetailsUiState.Success(property)
                    }
                    .onFailure { error ->
                        _uiState.value = PropertyDetailsUiState.Error(
                            error.message ?: "Failed to load property"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = PropertyDetailsUiState.Error(
                    e.message ?: "Failed to load property"
                )
            }
        }
    }

    fun refresh() {
        loadProperty()
    }

    fun contactLandlord(property: Property) {
        // TODO: Implement contact functionality
    }
}

sealed class PropertyDetailsUiState {
    data object Loading : PropertyDetailsUiState()
    data class Success(val property: Property) : PropertyDetailsUiState()
    data class Error(val message: String) : PropertyDetailsUiState()
}
