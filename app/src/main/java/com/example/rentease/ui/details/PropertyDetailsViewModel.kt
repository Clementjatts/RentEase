package com.example.rentease.ui.details

import androidx.lifecycle.SavedStateHandle
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
    private val repository: PropertyRepository,
    private val savedStateHandle: SavedStateHandle
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
                result.onSuccess { property ->
                    _uiState.value = PropertyDetailsUiState.Success(property)
                }.onFailure { error ->
                    _uiState.value = PropertyDetailsUiState.Error(error.message ?: "Failed to load property")
                }
            } catch (e: Exception) {
                _uiState.value = PropertyDetailsUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun deleteProperty(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = PropertyDetailsUiState.Loading
            try {
                repository.deleteProperty(propertyId).onSuccess {
                    onSuccess()
                }.onFailure { error ->
                    _uiState.value = PropertyDetailsUiState.Error(error.message ?: "Failed to delete property")
                }
            } catch (e: Exception) {
                _uiState.value = PropertyDetailsUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    class Factory(
        private val propertyId: Int,
        private val repository: PropertyRepository,
        private val savedStateHandle: SavedStateHandle
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PropertyDetailsViewModel::class.java)) {
                return PropertyDetailsViewModel(propertyId, repository, savedStateHandle) as T
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
