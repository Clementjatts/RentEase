package com.example.rentease.ui.properties

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.R
import com.example.rentease.auth.AuthManager
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

    private val _uiState = MutableStateFlow<PropertyListUiState>(PropertyListUiState.Loading)
    val uiState: StateFlow<PropertyListUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(PropertyFilter.ALL)
    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    private val _sortOption = MutableStateFlow(SortBottomSheetDialog.SortOption.NEWEST)

    val filteredProperties: StateFlow<List<Property>> = combine(
        _searchQuery,
        _selectedFilter,
        _properties,
        _sortOption
    ) { query, filter, properties, sortOption ->
        var filtered = properties.filter { property ->
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

        // Apply sorting
        filtered = when (sortOption) {
            SortBottomSheetDialog.SortOption.PRICE_LOW_TO_HIGH -> {
                filtered.sortedBy { it.price }
            }
            SortBottomSheetDialog.SortOption.PRICE_HIGH_TO_LOW -> {
                filtered.sortedByDescending { it.price }
            }
            SortBottomSheetDialog.SortOption.NEWEST -> {
                filtered.sortedByDescending { it.dateAdded }
            }
            SortBottomSheetDialog.SortOption.OLDEST -> {
                filtered.sortedBy { it.dateAdded }
            }
            SortBottomSheetDialog.SortOption.NEAREST -> {
                // TODO: Implement location-based sorting when location services are added
                filtered
            }
        }

        filtered
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
        return PropertyListUiState.Error(
            application.getString(R.string.error_unknown)
        )
    }

    fun loadProperties() {
        viewModelScope.launch {
            _uiState.value = PropertyListUiState.Loading

            try {
                repository.getProperties()
                    .onSuccess { properties ->
                        _properties.value = properties
                        _uiState.value = if (properties.isEmpty()) {
                            PropertyListUiState.Empty
                        } else {
                            PropertyListUiState.Success(properties)
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = handleApiError(error)
                    }
            } catch (e: Throwable) {
                _uiState.value = handleApiError(e)
            }
        }
    }

    private fun handleLoadPropertiesError(exception: Exception) {
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
                repository.getProperties(forceRefresh = true)
                    .onSuccess { properties ->
                        _properties.value = properties
                        _uiState.value = if (properties.isEmpty()) {
                            PropertyListUiState.Empty
                        } else {
                            PropertyListUiState.Success(properties)
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = handleApiError(error)
                    }
            } catch (e: Throwable) {
                _uiState.value = handleApiError(e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun handleDeleteError(exception: Exception) {
        viewModelScope.launch {
            _uiState.emit(
                PropertyListUiState.Error(
                    application.getString(R.string.error_unknown)
                )
            )
        }
    }

    fun deleteProperty(propertyId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteProperty(propertyId)
                    .onSuccess {
                        loadProperties()
                    }
                    .onFailure { error ->
                        _uiState.value = handleApiError(error)
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
                val authManager = AuthManager.getInstance(application)
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
