package com.example.rentease.ui.propertylist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the PropertyListFragment.
 * Handles loading and managing the list of properties.
 */
class PropertyListViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val propertyRepository = RepositoryProvider.providePropertyRepository(application)

    private val _uiState = MutableStateFlow<PropertyListUiState>(PropertyListUiState.Loading)
    val uiState: StateFlow<PropertyListUiState> = _uiState

    /**
     * Load all available properties from the repository.
     */
    fun loadProperties() {
        viewModelScope.launch {
            try {
                _uiState.value = PropertyListUiState.Loading
                when (val result = propertyRepository.getProperties()) {
                    is com.example.rentease.data.model.Result.Success -> {
                        _uiState.value = PropertyListUiState.Success(result.data)
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyListUiState.Error(result.errorMessage ?: "Failed to load properties")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PropertyListUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * Refresh properties (same as loadProperties but with clearer intent for pull-to-refresh).
     */
    fun refreshProperties() {
        loadProperties()
    }

    /**
     * Factory for creating PropertyListViewModel instances.
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PropertyListViewModel::class.java)) {
                return PropertyListViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Represents the UI state for the property list screen.
 */
sealed class PropertyListUiState {
    data object Loading : PropertyListUiState()
    data class Success(val properties: List<com.example.rentease.data.model.Property>) : PropertyListUiState()
    data class Error(val message: String) : PropertyListUiState()
} 