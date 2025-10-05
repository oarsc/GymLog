package org.oar.gymlog.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.content.ContextCompat
import org.oar.gymlog.R
import org.oar.gymlog.model.Variation
import org.oar.gymlog.ui.common.notifications.NotificationLoggingService
import org.oar.gymlog.util.DateUtils.isSystemTimePast

class NotificationService(private val context: Context) {

    companion object {
        const val COUNTDOWN_CHANNEL = "countdown"
        const val READY_CHANNEL = "ready"
        const val NOTIFICATION_COUNTDOWN_ID = 1
        const val NOTIFICATION_READY_ID = 2
        var lastEndTime: Long = 0L
            private set
    }

    fun startNewNotification(endTime: Long, seconds: Int, variation: Variation) {
        if (endTime.isSystemTimePast) return
        lastEndTime = endTime

        val intent = Intent(context, NotificationLoggingService::class.java)
        intent.putExtra("seconds", seconds)
        intent.putExtra("milliseconds", endTime)
        intent.putExtra("name", variation.exercise.name)
        intent.putExtra("variationId", variation.id)

        intent.action = NotificationLoggingService.ACTION_START
        ContextCompat.startForegroundService(context, intent)
    }

    fun editNotification(endTime: Long) {
        if (lastEndTime.isSystemTimePast && endTime.isSystemTimePast) {
            lastEndTime = endTime
            return
        }
        lastEndTime = endTime

        val intent = Intent(context, NotificationLoggingService::class.java)
        intent.putExtra("milliseconds", lastEndTime)
        intent.action = NotificationLoggingService.ACTION_REPLACE
        ContextCompat.startForegroundService(context, intent)
    }

    fun hideNotification() {
        val intent = Intent(context, NotificationLoggingService::class.java)
        context.stopService(intent)
        lastEndTime = 0L
    }

    fun createNotificationsChannel() {
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )
        val countdownChannel = NotificationChannel(COUNTDOWN_CHANNEL,
            context.getString(R.string.notification_title_countdown),
            NotificationManager.IMPORTANCE_DEFAULT)
            .apply {
                setSound(null, null)
            }

        notificationManager.createNotificationChannel(countdownChannel)

        val readyChannel = NotificationChannel(READY_CHANNEL,
            context.getString(R.string.notification_title_ready),
            NotificationManager.IMPORTANCE_HIGH)
            .apply {
                enableLights(true)
                lightColor = Color.MAGENTA
            }

        notificationManager.createNotificationChannel(readyChannel)
    }
}