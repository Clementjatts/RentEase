package com.example.rentease.ui.propertyform

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * PropertyFormViewModel handles the business logic for the property form screen.
 */
class PropertyFormViewModel(
    application: Application,
    private val propertyId: Int
) : AndroidViewModel(application) {
    
    private val propertyRepository = PropertyRepository.getInstance(application)
    private val authManager = AuthManager.getInstance(application)
    
    private val _uiState = MutableStateFlow<PropertyFormUiState>(PropertyFormUiState.Initial)
    val uiState: StateFlow<PropertyFormUiState> = _uiState
    
    private val _images = MutableStateFlow<List<Uri>>(emptyList())
    val images: StateFlow<List<Uri>> = _images
    
    /**
     * Load the property data from the repository.
     */
    fun loadPropertyDetails() {
        if (propertyId == -1) return // New property, don't load anything
        
        viewModelScope.launch {
            try {
                val result = propertyRepository.getPropertyById(propertyId)
                
                when (result) {
                    is com.example.rentease.data.model.Result.Success -> {
                        val property = result.data
                        _uiState.value = PropertyFormUiState.PropertyData(
                            title = property.title,
                            description = property.description ?: "",
                            address = property.address,
                            price = property.price.toString()
                        )
                        
                        // Load images
                        property.images?.let { imageUrls ->
                            _images.value = imageUrls.mapNotNull { Uri.parse(it) }
                        }
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyFormUiState.Error(result.errorMessage ?: "Failed to load property")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PropertyFormUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
    
    /**
     * Save the property to the repository.
     */
    fun saveProperty(title: String, description: String, address: String, price: String) {
        if (title.isBlank() || description.isBlank() || address.isBlank() || price.isBlank()) {
            _uiState.value = PropertyFormUiState.Error("All fields are required")
            return
        }
        
        _uiState.value = PropertyFormUiState.Loading
        
        viewModelScope.launch {
            try {
                val landlordId = authManager.getUserId().toInt()
                
                var result: com.example.rentease.data.model.Result<Property>
                
                if (propertyId == -1) {
                    // Create new property
                    val newProperty = Property(
                        id = 0,
                        title = title,
                        description = description,
                        address = address,
                        price = java.math.BigDecimal(price),
                        landlordId = landlordId,
                        images = _images.value.map { it.toString() }
                    )
                    result = propertyRepository.createProperty(newProperty)
                } else {
                    // Update existing property
                    val updatedProperty = Property(
                        id = propertyId,
                        title = title,
                        description = description,
                        address = address,
                        price = java.math.BigDecimal(price),
                        landlordId = landlordId,
                        images = _images.value.map { it.toString() }
                    )
                    result = propertyRepository.updateProperty(updatedProperty)
                }
                
                when (result) {
                    is com.example.rentease.data.model.Result.Success -> {
                        _uiState.value = PropertyFormUiState.Success
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyFormUiState.Error(result.errorMessage ?: "Failed to save property")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PropertyFormUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
    
    /**
     * Add an image to the property.
     */
    fun addImage(uri: Uri) {
        val currentImages = _images.value.toMutableList()
        currentImages.add(uri)
        _images.value = currentImages
    }
    
    /**
     * Remove an image from the property.
     */
    fun removeImage(position: Int) {
        val currentImages = _images.value.toMutableList()
        if (position in currentImages.indices) {
            currentImages.removeAt(position)
            _images.value = currentImages
        }
    }
    
    /**
     * Factory for creating PropertyFormViewModel instances.
     */
    class Factory(
        private val application: Application,
        private val propertyId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PropertyFormViewModel::class.java)) {
                return PropertyFormViewModel(application, propertyId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Represents the UI state for the property form screen.
 */
sealed class PropertyFormUiState {
    object Initial : PropertyFormUiState()
    object Loading : PropertyFormUiState()
    object Success : PropertyFormUiState()
    data class Error(val message: String) : PropertyFormUiState()
    data class PropertyData(
        val title: String,
        val description: String,
        val address: String,
        val price: String
    ) : PropertyFormUiState()
}
