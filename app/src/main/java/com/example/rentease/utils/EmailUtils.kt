package com.example.rentease.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * Utility class for handling email operations
 */
object EmailUtils {
    
    /**
     * Send an email using the device's email client
     * 
     * @param context The application context
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body content
     * @return Boolean indicating if the email intent was successfully launched
     */
    fun sendEmail(context: Context, to: String, subject: String, body: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                Log.e("EmailUtils", "No email client found")
                false
            }
        } catch (e: Exception) {
            Log.e("EmailUtils", "Error sending email", e)
            false
        }
    }
    
    /**
     * Format a property inquiry email body
     */
    fun formatPropertyInquiryBody(
        propertyId: Int,
        propertyTitle: String,
        name: String,
        email: String,
        message: String
    ): String {
        return """
            |Property Inquiry
            |---------------
            |Property ID: $propertyId
            |Property: $propertyTitle
            |
            |From: $name
            |Email: $email
            |
            |Message:
            |$message
            |
            |This inquiry was sent through the RentEase application.
        """.trimMargin()
    }
    
    /**
     * Format a general contact form email body
     */
    fun formatContactFormBody(
        name: String,
        email: String,
        message: String
    ): String {
        return """
            |Contact Form Submission
            |---------------------
            |From: $name
            |Email: $email
            |
            |Message:
            |$message
            |
            |This message was sent through the RentEase application.
        """.trimMargin()
    }
}