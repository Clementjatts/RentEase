package com.example.rentease.ui.propertyform

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class PropertyFormViewModel(
    private val propertyId: Int?,
    private val repository: PropertyRepository,
    private val authManager: AuthManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<PropertyFormUiState>(PropertyFormUiState.Initial)
    val uiState: StateFlow<PropertyFormUiState> = _uiState

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    init {
        if (propertyId != null) {
            loadProperty()
        }
    }

    private fun loadProperty() {
        viewModelScope.launch {
            _uiState.value = PropertyFormUiState.Loading
            try {
                repository.getProperty(propertyId!!).onSuccess { property ->
                    _uiState.value = PropertyFormUiState.Success(property)
                }.onFailure { error ->
                    _uiState.value = PropertyFormUiState.Error(error.message ?: "Failed to load property")
                }
            } catch (e: Exception) {
                _uiState.value = PropertyFormUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun saveProperty(
        title: String,
        description: String,
        address: String,
        price: String
    ) {
        if (!validateInput(title, description, address, price)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = PropertyFormUiState.Loading
            try {
                val property = Property(
                    id = propertyId ?: 0,
                    title = title,
                    description = description,
                    address = address,
                    price = BigDecimal(price),
                    landlordId = 1, // TODO: Get from AuthManager
                    landlordName = null,
                    landlordContact = null,
                    images = emptyList() // Images will be uploaded separately
                )

                val result = if (propertyId == null) {
                    repository.createProperty(property, _selectedImages.value)
                } else {
                    repository.updateProperty(property, _selectedImages.value)
                }

                result.onSuccess {
                    _uiState.value = PropertyFormUiState.Saved
                }.onFailure { error ->
                    _uiState.value = PropertyFormUiState.Error(error.message ?: "Failed to save property")
                }
            } catch (e: Exception) {
                _uiState.value = PropertyFormUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun addImage(uri: Uri) {
        val currentList = _selectedImages.value.toMutableList()
        currentList.add(uri)
        _selectedImages.value = currentList
    }

    fun removeImage(uri: Uri) {
        val currentList = _selectedImages.value.toMutableList()
        currentList.remove(uri)
        _selectedImages.value = currentList
    }

    private fun validateInput(
        title: String,
        description: String,
        address: String,
        price: String
    ): Boolean {
        if (title.isBlank() || description.isBlank() || address.isBlank() || price.isBlank()) {
            _uiState.value = PropertyFormUiState.Error("All fields are required")
            return false
        }

        try {
            BigDecimal(price)
        } catch (e: NumberFormatException) {
            _uiState.value = PropertyFormUiState.Error("Please enter a valid price")
            return false
        }

        return true
    }

    class Factory(
        private val propertyId: Int?,
        private val repository: PropertyRepository,
        private val authManager: AuthManager,
        private val savedStateHandle: SavedStateHandle
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PropertyFormViewModel::class.java)) {
                return PropertyFormViewModel(propertyId, repository, authManager, savedStateHandle) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class PropertyFormUiState {
    data object Initial : PropertyFormUiState()
    data object Loading : PropertyFormUiState()
    data class Success(val property: Property) : PropertyFormUiState()
    data object Saved : PropertyFormUiState()
    data class Error(val message: String) : PropertyFormUiState()
}
