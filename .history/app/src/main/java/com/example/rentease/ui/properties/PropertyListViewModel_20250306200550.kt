package com.example.rentease.ui.properties

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.R
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import com.example.rentease.ui.propertylist.SortBottomSheetDialog
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(FlowPreview::class)
class PropertyListViewModel(
    private val repository: PropertyRepository,
    private val application: Application
) : ViewModel() {

    private val TAG = "PropertyListViewModel"

    private val _uiState = MutableStateFlow<PropertyListUiState>(PropertyListUiState.Loading)
    val uiState: StateFlow<PropertyListUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(PropertyFilter.ALL)
    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    private val _sortOption = MutableStateFlow(SortBottomSheetDialog.SortOption.NEWEST)

    // Use a more efficient approach with debounce to avoid excessive processing
    val filteredProperties: StateFlow<List<Property>> = combine(
        _searchQuery.debounce(300), // Add debounce to avoid processing every keystroke
        _selectedFilter,
        _properties,
        _sortOption
    ) { query, filter, properties, sortOption ->
        // Use a sequence for more efficient filtering
        var filtered = properties.asSequence().filter { property ->
            if (query.isBlank()) return@filter true

            when (filter) {
                PropertyFilter.ALL -> {
                    property.title.contains(query, ignoreCase = true) ||
                    property.description?.contains(query, ignoreCase = true) ?: false ||
                    property.address.contains(query, ignoreCase = true)
                }
                PropertyFilter.PRICE -> {
                    try {
                        val queryPrice = BigDecimal(query)
                        property.price <= queryPrice
                    } catch (e: Exception) {
                        false
                    }
                }
                PropertyFilter.LOCATION -> {
                    property.address.contains(query, ignoreCase = true)
                }
            }
        }

        // Apply sorting - ensure all branches return List<Property>
        when (sortOption) {
            SortBottomSheetDialog.SortOption.PRICE_LOW_TO_HIGH ->
                filtered.sortedBy { it.price }.toList()
            SortBottomSheetDialog.SortOption.PRICE_HIGH_TO_LOW ->
                filtered.sortedByDescending { it.price }.toList()
            SortBottomSheetDialog.SortOption.NEWEST ->
                filtered.sortedByDescending { it.dateAdded }.toList()
            SortBottomSheetDialog.SortOption.OLDEST ->
                filtered.sortedBy { it.dateAdded }.toList()
        }

        sortedList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadProperties()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: PropertyFilter) {
        _selectedFilter.value = filter
    }

    fun setSortOption(option: SortBottomSheetDialog.SortOption) {
        _sortOption.value = option
    }

    fun getCurrentSortOption() = _sortOption.value

    private fun handleApiError(exception: Throwable): PropertyListUiState {
        // Log the exception for debugging purposes
        Log.e(TAG, "API Error", exception)
        return PropertyListUiState.Error(
            application.getString(R.string.error_unknown)
        )
    }

    fun loadProperties() {
        viewModelScope.launch {
            _uiState.value = PropertyListUiState.Loading

            try {
                val result = repository.getProperties()
                when (result) {
                    is com.example.rentease.data.model.Result.Success -> {
                        val properties = result.data
                        _properties.value = properties
                        _uiState.value = if (properties.isEmpty()) {
                            PropertyListUiState.Empty
                        } else {
                            PropertyListUiState.Success(properties)
                        }
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyListUiState.Error(result.errorMessage ?: "Unknown error")
                    }
                }
            } catch (e: Throwable) {
                _uiState.value = handleApiError(e)
            }
        }
    }

    private fun handleLoadPropertiesError(exception: Exception) {
        // Log the exception for debugging purposes
        Log.e(TAG, "Load Properties Error", exception)
        viewModelScope.launch {
            _uiState.emit(
                PropertyListUiState.Error(
                    application.getString(R.string.error_unknown)
                )
            )
        }
    }

    private fun handleDeleteError(exception: Exception) {
        // Log the exception for debugging purposes
        Log.e(TAG, "Delete Property Error", exception)
        viewModelScope.launch {
            _uiState.emit(
                PropertyListUiState.Error(
                    application.getString(R.string.error_unknown)
                )
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val result = repository.getProperties(forceRefresh = true)
                when (result) {
                    is com.example.rentease.data.model.Result.Success -> {
                        val properties = result.data
                        _properties.value = properties
                        _uiState.value = if (properties.isEmpty()) {
                            PropertyListUiState.Empty
                        } else {
                            PropertyListUiState.Success(properties)
                        }
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyListUiState.Error(result.errorMessage ?: "Unknown error")
                    }
                }
            } catch (e: Throwable) {
                _uiState.value = handleApiError(e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun deleteProperty(propertyId: Int) {
        viewModelScope.launch {
            try {
                val result = repository.deleteProperty(propertyId)
                when (result) {
                    is com.example.rentease.data.model.Result.Success -> {
                        loadProperties()
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyListUiState.Error(result.errorMessage ?: "Unknown error")
                    }
                }
            } catch (e: Throwable) {
                _uiState.value = handleApiError(e)
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PropertyListViewModel::class.java)) {
                val repository = PropertyRepository(ApiClient.api, application)
                return PropertyListViewModel(repository, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

enum class PropertyFilter {
    ALL,
    PRICE,
    LOCATION
}

sealed class PropertyListUiState {
    data object Loading : PropertyListUiState()
    data object Empty : PropertyListUiState()
    data class Success(val properties: List<Property>) : PropertyListUiState()
    data class Error(val message: String) : PropertyListUiState()
}
