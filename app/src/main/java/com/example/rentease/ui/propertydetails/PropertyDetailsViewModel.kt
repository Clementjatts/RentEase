package com.example.rentease.ui.propertydetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PropertyDetailsViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val propertyRepository = PropertyRepository(application)
    private val propertyId: String = checkNotNull(savedStateHandle["property_id"])

    private val _uiState = MutableStateFlow<PropertyDetailsUiState>(PropertyDetailsUiState.Loading)
    val uiState: StateFlow<PropertyDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProperty()
    }

    private fun loadProperty() {
        viewModelScope.launch {
            try {
                val property = propertyRepository.getProperty(propertyId)
                _uiState.value = PropertyDetailsUiState.Success(property)
            } catch (e: Exception) {
                _uiState.value = PropertyDetailsUiState.Error(e.message ?: "Failed to load property")
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
