package org.scp.gymlog.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import org.scp.gymlog.R
import org.scp.gymlog.model.Variation
import org.scp.gymlog.ui.common.notifications.NotificationLoggingService
import org.scp.gymlog.util.Constants.DATE_ZERO
import org.scp.gymlog.util.DateUtils.isPast
import org.scp.gymlog.util.DateUtils.timeInMillis
import java.time.LocalDateTime

class NotificationService(private val context: Context) {

    companion object {
        const val COUNTDOWN_CHANNEL = "countdown"
        const val READY_CHANNEL = "ready"
        const val NOTIFICATION_COUNTDOWN_ID = 1
        const val NOTIFICATION_READY_ID = 2
        var lastEndTime: LocalDateTime = DATE_ZERO
            private set
    }

    fun startNewNotification(endTime: LocalDateTime, seconds: Int, variation: Variation) {
        if (endTime.isPast) return
        lastEndTime = endTime

        val intent = Intent(context, NotificationLoggingService::class.java)
        intent.putExtra("seconds", seconds)
        intent.putExtra("milliseconds", endTime.timeInMillis)
        intent.putExtra("name", variation.exercise.name)
        intent.putExtra("variationId", variation.id)

        context.stopService(intent)
        intent.action = NotificationLoggingService.ACTION_START
        context.startService(intent)
    }

    fun editNotification(endTime: LocalDateTime) {
        if (lastEndTime.isPast && endTime.isPast) {
            lastEndTime = endTime
            return
        }
        lastEndTime = endTime

        val intent = Intent(context, NotificationLoggingService::class.java)
        intent.putExtra("milliseconds", lastEndTime.timeInMillis)
        intent.action = NotificationLoggingService.ACTION_REPLACE
        context.startService(intent)
    }

    fun hideNotification() {
        val intent = Intent(context, NotificationLoggingService::class.java)
        context.stopService(intent)
        intent.action = NotificationLoggingService.ACTION_STOP
        context.startService(intent)
        lastEndTime = DATE_ZERO
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

        countdownChannel.setSound(null, null)
        notificationManager.createNotificationChannel(countdownChannel)

        val readyChannel = NotificationChannel(READY_CHANNEL,
            context.getString(R.string.notification_title_ready),
            NotificationManager.IMPORTANCE_HIGH)

        notificationManager.createNotificationChannel(readyChannel)
    }
}