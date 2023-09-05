package org.scp.gymlog.ui.common.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
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
        fun close(service: Service) {
            service.stopForeground(true)
        }
    }

    private lateinit var notification: Notification
    private val remoteView = RemoteViews(service.packageName, R.layout.notification_countdown)

    var exerciseName = exerciseName
        set(value) {
            remoteView.setTextViewText(R.id.exerciseName, value)
            field = value
        }

    var maxTime = 10
        set(value) {
            remoteView.setInt(R.id.progressBar, "setMax", value)
            field = value
        }

    var remainingTime = 0
        set(value) {
            remoteView.setInt(R.id.progressBar, "setProgress", value)
            remoteView.setTextViewText(R.id.seconds, (value/1000f).roundToInt().toString())
            field = value
        }

    init {
        remoteView.setTextViewText(R.id.exerciseName, exerciseName)
    }

    fun showNotification() {
        notification = build()
        service.startForeground(NotificationService.NOTIFICATION_COUNTDOWN_ID, notification)
    }

    fun update() {
        val notificationManager = service.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationService.NOTIFICATION_COUNTDOWN_ID, notification)
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