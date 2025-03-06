package com.example.rentease.data.repository

import android.content.Context
import android.util.Log
import com.example.rentease.data.api.RentEaseApi
import com.example.rentease.data.model.ApiResponse
import com.example.rentease.data.model.Result
import com.example.rentease.utils.EmailUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for handling property inquiry and contact form requests
 */
class RequestRepository(
    private val api: RentEaseApi,
    private val context: Context
) {
    /**
     * Submit a property inquiry to a landlord
     * This forwards the inquiry directly to the landlord's email
     */
    suspend fun submitPropertyInquiry(
        userId: String,
        propertyId: Int,
        landlordId: String,
        name: String,
        email: String,
        subject: String,
        message: String
    ): Result<Unit> {
        return try {
            // Get landlord email and property title
            val landlordEmail = getLandlordEmail(landlordId)
            val propertyTitle = getPropertyTitle(propertyId)
            
            // Format email body
            val emailBody = EmailUtils.formatPropertyInquiryBody(
                propertyId = propertyId,
                propertyTitle = propertyTitle,
                name = name,
                email = email,
                message = message
            )
            
            // Send email to landlord
            val emailSent = withContext(Dispatchers.Main) {
                EmailUtils.sendEmail(
                    context = context,
                    to = landlordEmail,
                    subject = "Property Inquiry: $subject",
                    body = emailBody
                )
            }
            
            Log.d("RequestRepository", "Property inquiry submitted - User: $userId, Property: $propertyId, Landlord: $landlordId, Email sent: $emailSent")
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("RequestRepository", "Error submitting property inquiry", e)
            Result.Error("Failed to submit inquiry: ${e.message}")
        }
    }
    
    /**
     * Submit a general contact form
     * This forwards the form directly to the admin email
     */
    suspend fun submitContactForm(
        userId: String,
        name: String,
        email: String,
        subject: String,
        message: String
    ): Result<Unit> {
        return try {
            // Get admin email
            val adminEmail = getAdminEmail()
            
            // Format email body
            val emailBody = EmailUtils.formatContactFormBody(
                name = name,
                email = email,
                message = message
            )
            
            // Send email to admin
            val emailSent = withContext(Dispatchers.Main) {
                EmailUtils.sendEmail(
                    context = context,
                    to = adminEmail,
                    subject = "Contact Form: $subject",
                    body = emailBody
                )
            }
            
            Log.d("RequestRepository", "Contact form submitted - User: $userId, Email sent: $emailSent")
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("RequestRepository", "Error submitting contact form", e)
            Result.Error("Failed to submit form: ${e.message}")
        }
    }
    
    /**
     * Submit a request (for maintenance, viewing, etc.)
     */
    suspend fun submitRequest(request: Any): Result<Unit> {
        return try {
            // In a real implementation, this would send the request to the API
            // For now, we'll just log it and return success
            Log.d("RequestRepository", "Request submitted: $request")
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("RequestRepository", "Error submitting request", e)
            Result.Error("Failed to submit request: ${e.message}")
        }
    }
    
    /**
     * Get landlord email from landlord ID
     * In a real app, this would query the database or API
     */
    private suspend fun getLandlordEmail(landlordId: String): String {
        // In a real implementation, this would fetch the landlord's email from the API
        // For now, we'll return a placeholder email
        return "landlord$landlordId@example.com"
    }
    
    /**
     * Get property title from property ID
     * In a real app, this would query the database or API
     */
    private suspend fun getPropertyTitle(propertyId: Int): String {
        // In a real implementation, this would fetch the property title from the API
        // For now, we'll return a placeholder title
        return "Property #$propertyId"
    }
    
    /**
     * Get admin email
     * In a real app, this would be configured in settings
     */
    private fun getAdminEmail(): String {
        // In a real implementation, this would fetch the admin email from settings
        // For now, we'll return a placeholder email
        return "admin@rentease.com"
    }
}
