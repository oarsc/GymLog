package org.scp.gymlog.ui.common.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import org.scp.gymlog.R
import org.scp.gymlog.SplashActivity
import org.scp.gymlog.service.NotificationService
import java.io.Closeable
import kotlin.math.roundToInt

open class CountdownNotification (
    private val service: Service,
    exerciseName: String,
    private val startTime: Long,
    private val variationId: Int
): Closeable {

    companion object {
        private const val GREEN = 0
        private const val ORANGE = 1
        private const val RED = 2

        fun close(service: Service) {
            service.stopForeground(true)
        }
    }

    private lateinit var notification: Notification
    private val remoteView = RemoteViews(service.packageName, R.layout.notification_countdown)

    var maxTime = 1
        set(value) {
            field = value
            remoteView.setInt(R.id.progressBarG, "setMax", value)
            remoteView.setInt(R.id.progressBarO, "setMax", value)
            remoteView.setInt(R.id.progressBarR, "setMax", value)
        }

    var remainingTime = 1
        set(value) {
            field = value

            val color = activeColor
            val progressBar = when(color) {
                GREEN -> R.id.progressBarG
                ORANGE -> R.id.progressBarO
                else -> R.id.progressBarR
            }
            remoteView.setInt(progressBar, "setProgress", value)

            val seconds = when(color) {
                GREEN -> R.id.secondsG
                ORANGE -> R.id.secondsO
                else -> R.id.secondsR
            }
            val text = String.format(
                service.getString(R.string.compound_notification_countdown),
                (value/1000f).roundToInt(),
            )
            remoteView.setTextViewText(seconds, text)

            updateColor(color)
        }

    private var visibleColor = -1
    private val activeColor: Int
        get() {
            val percent = remainingTime / maxTime.toFloat()
            return when {
                remainingTime < 10500 || percent < 0.1 -> RED
                percent < 0.5 -> ORANGE
                else -> GREEN
            }
        }

    init {
        remoteView.setTextViewText(R.id.exerciseNameG, exerciseName)
        remoteView.setTextViewText(R.id.exerciseNameO, exerciseName)
        remoteView.setTextViewText(R.id.exerciseNameR, exerciseName)
    }

    fun showNotification() {
        notification = build()
        service.startForeground(NotificationService.NOTIFICATION_COUNTDOWN_ID, notification)
    }

    fun update() {
        val notificationManager = service.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationService.NOTIFICATION_COUNTDOWN_ID, notification)
    }

    private fun updateColor(color: Int) {
        if (visibleColor == color) return
        visibleColor = color

        when (color) {
            GREEN -> {
                remoteView.setViewVisibility(R.id.green, View.VISIBLE)
                remoteView.setViewVisibility(R.id.orange, View.GONE)
                remoteView.setViewVisibility(R.id.red, View.GONE)
            }
            ORANGE -> {
                remoteView.setViewVisibility(R.id.green, View.GONE)
                remoteView.setViewVisibility(R.id.orange, View.VISIBLE)
                remoteView.setViewVisibility(R.id.red, View.GONE)
            }
            RED -> {
                remoteView.setViewVisibility(R.id.green, View.GONE)
                remoteView.setViewVisibility(R.id.orange, View.GONE)
                remoteView.setViewVisibility(R.id.red, View.VISIBLE)
            }
        }
    }

    override fun close() = close(service)

    private fun build(): Notification {
        return NotificationCompat.Builder(service, NotificationService.COUNTDOWN_CHANNEL)
            .setSmallIcon(R.drawable.ic_logo_24dp)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteView)
            .setOngoing(true)
            .setUsesChronometer(true)
            .setWhen(startTime)
            .setContentIntent(
                PendingIntent.getActivity(
                    service, 0, getStartAppIntent(service, variationId),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()
    }

    private fun getStartAppIntent(context: Context, variationId: Int = -1) : Intent {
        val startAppIntent = Intent(context, SplashActivity::class.java)
        startAppIntent.action = Intent.ACTION_MAIN
        startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (variationId >= 0) {
            startAppIntent.putExtra("variationId", variationId)
        }
        return startAppIntent
    }
}