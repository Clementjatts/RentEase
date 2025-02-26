package com.example.rentease.ui.propertydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rentease.data.repository.PropertyRepository

class PropertyDetailsViewModelFactory(
    private val repository: PropertyRepository,
    private val propertyId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertyDetailsViewModel::class.java)) {
            return PropertyDetailsViewModel(repository, propertyId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
