package com.example.silentshield.utils


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.silentshield.MainActivity
import com.example.silentshield.R

object NotificationHelper {

    private const val ALERT_CHANNEL_ID      = "scam_alerts"
    private const val FOREGROUND_CHANNEL_ID = "shield_foreground"
    const val FOREGROUND_NOTIFICATION_ID    = 1001

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        // High priority channel for scam alerts
        val alertChannel = NotificationChannel(
            ALERT_CHANNEL_ID,
            "Scam Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Real-time alerts for scam calls and messages"
        }

        // Low priority channel for the foreground service
        val foregroundChannel = NotificationChannel(
            FOREGROUND_CHANNEL_ID,
            "Shield Active",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows while SilentShield is protecting your device"
        }

        manager.createNotificationChannel(alertChannel)
        manager.createNotificationChannel(foregroundChannel)
    }

    // Shown permanently while the foreground service runs
    fun buildForegroundNotification(context: Context) =
        NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle("SilentShield Active")
            .setContentText("Monitoring calls and SMS for scams")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    // Shown when a scam call or SMS is detected
    fun showScamAlert(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }
}