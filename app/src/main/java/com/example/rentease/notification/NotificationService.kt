package com.example.rentease.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.rentease.R
import com.example.rentease.MainActivity

/**
 * Service to handle notifications in the app
 */
class NotificationService(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "rentease_notifications"
        private const val CHANNEL_NAME = "RentEase Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for RentEase app"
        
        private const val REQUEST_NOTIFICATION_ID = 1001
        private const val PROPERTY_NOTIFICATION_ID = 2001
        
        @Volatile
        private var instance: NotificationService? = null
        
        fun getInstance(context: Context): NotificationService {
            return instance ?: synchronized(this) {
                instance ?: NotificationService(context.applicationContext).also { instance = it }
            }
        }
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Send a notification to a landlord about a new contact request
     * @param landlordId The ID of the landlord to notify
     */
    fun notifyLandlordAboutRequest(landlordId: Int, propertyTitle: String, requesterName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "landlord_requests")
            putExtra("landlord_id", landlordId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Contact Request")
            .setContentText("$requesterName is interested in your property: $propertyTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(REQUEST_NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Handle missing notification permission
                // This would be better handled with a permissions check before attempting notification
            }
        }
    }
    
    /**
     * Send a notification to a landlord about a property update
     * @param landlordId The ID of the landlord to notify
     */
    fun notifyLandlordAboutPropertyUpdate(landlordId: Int, propertyTitle: String, updateType: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "landlord_properties")
            putExtra("landlord_id", landlordId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Property Update")
            .setContentText("Your property $propertyTitle has been $updateType")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(PROPERTY_NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Handle missing notification permission
                // This would be better handled with a permissions check before attempting notification
            }
        }
    }
    
    /**
     * Show a notification that a request has been submitted
     */
    fun showRequestSubmittedNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "propertyListFragment")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Request Submitted")
            .setContentText("Your request has been submitted successfully")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(REQUEST_NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Handle missing notification permission
            }
        }
    }
}
