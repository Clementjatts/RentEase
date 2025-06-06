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
 * Utility class to handle notifications in the app
 * Uses static methods to avoid memory leaks from holding Context references
 */
object NotificationService {

    private const val CHANNEL_ID = "rentease_notifications"
    private const val CHANNEL_NAME = "RentEase Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for RentEase app"

    private const val REQUEST_NOTIFICATION_ID = 1001

    private fun createNotificationChannel(context: Context) {
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
     * Show a notification that a request has been submitted
     * @param context The context to use for creating the notification
     */
    fun showRequestSubmittedNotification(context: Context) {
        // Ensure notification channel is created
        createNotificationChannel(context)

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
