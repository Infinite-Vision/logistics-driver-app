package com.example.logistics_driver_app.Notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.logistics_driver_app.MainActivity
import com.example.logistics_driver_app.R

/**
 * NotificationHelper - Helper class for managing push notifications.
 * Creates and displays notifications for various app events.
 */
class NotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        private const val CHANNEL_ID_TRIP = "trip_notifications"
        private const val CHANNEL_ID_GENERAL = "general_notifications"
        private const val CHANNEL_ID_EMERGENCY = "emergency_notifications"
        
        private const val NOTIFICATION_ID_TRIP = 1001
        private const val NOTIFICATION_ID_GENERAL = 1002
        private const val NOTIFICATION_ID_EMERGENCY = 1003
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Create notification channels for Android O and above.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Trip notifications channel
            val tripChannel = NotificationChannel(
                CHANNEL_ID_TRIP,
                "Trip Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about trip updates and navigation"
            }
            
            // General notifications channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }
            
            // Emergency notifications channel
            val emergencyChannel = NotificationChannel(
                CHANNEL_ID_EMERGENCY,
                "Emergency Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important and urgent notifications"
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(tripChannel)
            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(emergencyChannel)
        }
    }
    
    /**
     * Show trip notification.
     * @param title Notification title
     * @param message Notification message
     */
    fun showTripNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TRIP)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_TRIP, notification)
    }
    
    /**
     * Show general notification.
     * @param title Notification title
     * @param message Notification message
     */
    fun showGeneralNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_GENERAL, notification)
    }
    
    /**
     * Show emergency notification.
     * @param title Notification title
     * @param message Notification message
     */
    fun showEmergencyNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EMERGENCY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_EMERGENCY, notification)
    }
    
    /**
     * Cancel all notifications.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * Cancel specific notification.
     * @param notificationId Notification ID to cancel
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
