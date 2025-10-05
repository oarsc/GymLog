package org.oar.gymlog.ui.common.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import org.oar.gymlog.R
import org.oar.gymlog.service.NotificationService
import org.oar.gymlog.ui.main.MainActivity
import java.io.Closeable

class ReadyNotification (
    private val context: Context,
    exerciseName: String,
    private val startTime: Long,
    private val variationId: Int
): Closeable {

    companion object {
        fun close(context: Context) {
            val notificationManager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NotificationService.NOTIFICATION_READY_ID)
        }
    }

    private val remoteView = RemoteViews(context.packageName, R.layout.notification_ready)

    init {
        remoteView.setTextViewText(R.id.exerciseName, exerciseName)
    }

    fun showNotification() {
        val notification = build()
        val notificationManager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationService.NOTIFICATION_READY_ID, notification)
    }

    override fun close() = close(context)

    private fun build(): Notification {
        return NotificationCompat.Builder(context.applicationContext, NotificationService.READY_CHANNEL)
            .setSmallIcon(R.drawable.ic_logo_24dp)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.notification_rest_finished))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteView)
            .setUsesChronometer(true)
            .setWhen(startTime)
            //.setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context.applicationContext, 0, getStartAppIntent(context, variationId),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()
    }

    private fun getStartAppIntent(context: Context, variationId: Int = -1): Intent {
        val startAppIntent = Intent(context, MainActivity::class.java)
        startAppIntent.action = Intent.ACTION_MAIN
        startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (variationId >= 0) {
            startAppIntent.putExtra("variationId", variationId)
        }
        return startAppIntent
    }
}