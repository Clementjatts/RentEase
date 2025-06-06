package com.example.rentease.data.repository

import android.util.Log
import com.example.rentease.data.model.Result

// Repository for handling property inquiry requests
class RequestRepository : BaseRepository() {
    // Submit property inquiry to landlord via email
    suspend fun submitPropertyInquiry(
        propertyTitle: String,
        landlordContact: String,
        propertyId: Int,
        name: String,
        email: String,
        phone: String?,
        message: String
    ): Result<Unit> {
        return try {
            // Simple email simulation
            Log.i("EmailSent", "Property inquiry sent to: $landlordContact")
            Log.i("EmailSent", "Subject: Property Inquiry - $propertyTitle")
            Log.i("EmailSent", "From: $name ($email)")

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to send inquiry: ${e.message}")
        }
    }
}
