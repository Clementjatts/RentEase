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

    val filteredProperties: StateFlow<List<Property>> = combine(
        _searchQuery,
        _selectedFilter,
        _properties
    ) { query, filter, properties ->
        properties
            .filter { property ->
                if (query.isBlank()) return@filter true

                when (filter) {
                    PropertyFilter.ALL -> {
                        property.title.contains(query, ignoreCase = true) ||
                        property.description.contains(query, ignoreCase = true) ||
                        property.address.contains(query, ignoreCase = true)
                    }
                    PropertyFilter.PRICE -> {
                        try {
                            val queryPrice = BigDecimal(query)
                            property.price <= queryPrice
                        } catch (e: NumberFormatException) {
                            false
                        }
                    }
                    PropertyFilter.LOCATION -> {
                        property.address.contains(query, ignoreCase = true)
                    }
                }
            }
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
                        _uiState.value = PropertyListUiState.Error(
                            error.message ?: application.getString(R.string.error_loading_properties)
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = PropertyListUiState.Error(
                    e.message ?: application.getString(R.string.error_unknown)
                )
            }
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
                        _uiState.value = PropertyListUiState.Error(
                            error.message ?: application.getString(R.string.error_loading_properties)
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = PropertyListUiState.Error(
                    e.message ?: application.getString(R.string.error_unknown)
                )
            } finally {
                _isRefreshing.value = false
            }
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
                        _uiState.value = PropertyListUiState.Error(
                            error.message ?: application.getString(R.string.error_delete_property)
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = PropertyListUiState.Error(
                    e.message ?: application.getString(R.string.error_unknown)
                )
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
