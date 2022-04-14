package org.scp.gymlog.ui.common.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import org.scp.gymlog.R
import org.scp.gymlog.SplashActivity
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.util.DateUtils.diff
import org.scp.gymlog.util.DateUtils.isPast
import java.util.*
import kotlin.math.roundToInt

class NotificationLoggingService : Service() {

    companion object {
        const val ACTION_STOP = "stop"
    }

    private val mBinder: IBinder = Binder()
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    private lateinit var countdownRemoteView: RemoteViews
    private var notification: Notification? = null
    private var thread: Thread? = null
    private var startTime: Long = 0
    private var exerciseName: String? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ACTION_STOP == intent.action) {
            stopSelfResult(startId)
            return START_NOT_STICKY
        }
        getNotificationManager().cancel(NotificationService.NOTIFICATION_READY_ID)
        val endDate = Calendar.getInstance()
        exerciseName = intent.getStringExtra("name")
        val milliseconds = intent.getLongExtra("milliseconds", 0)
        val seconds = intent.getIntExtra("seconds", 10)
        if (milliseconds > 0) {
            endDate.timeInMillis = milliseconds
            val startDate = endDate.clone() as Calendar
            startDate.add(Calendar.SECOND, -seconds)
            startTime = startDate.timeInMillis
        } else {
            startTime = endDate.timeInMillis
            endDate.add(Calendar.SECOND, seconds)
        }
        if (seconds <= 0) {
            showReadyNotification()
            stopSelf()
        } else {
            startForeground(
                NotificationService.NOTIFICATION_COUNTDOWN_ID,
                generateCountdownNotification(seconds).also { notification = it })
            thread = Thread {
                var endedNaturally = false
                try {
                    while (!endDate.isPast) {
                        val diff = endDate.diff().toInt()
                        updateNotification(diff)
                        Thread.sleep(500)
                    }
                    endedNaturally = true
                } catch (e: InterruptedException) {
                    // Interrupted
                } finally {
                    stopForeground(true)
                    if (endedNaturally) {
                        showReadyNotification()
                        stopSelf()
                    }
                }
            }
            thread!!.start()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        if (thread != null) {
            thread!!.interrupt()
            thread = null
        }
        super.onDestroy()
    }

    private fun generateCountdownNotification(maxSeconds: Int): Notification {
        countdownRemoteView = RemoteViews(packageName, R.layout.notification_countdown)
        countdownRemoteView.setInt(R.id.progressBar, "setMax", maxSeconds * 1000)
        countdownRemoteView.setTextViewText(R.id.exerciseName, exerciseName)
        val builder = NotificationCompat.Builder(this, NotificationService.COUNTDOWN_CHANNEL)
            .setSmallIcon(R.drawable.ic_logo_24dp)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(countdownRemoteView)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, getStartAppIntent(),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        return builder.build()
    }

    private fun updateNotification(remainingSeconds: Int) {
        updateViewSeconds(remainingSeconds)
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NotificationService.NOTIFICATION_COUNTDOWN_ID, notification)
    }

    private fun updateViewSeconds(remainingSeconds: Int) {
        countdownRemoteView.setInt(R.id.progressBar, "setProgress", remainingSeconds)
        countdownRemoteView.setTextViewText(R.id.seconds, (remainingSeconds/1000.0).roundToInt().toString())
    }

    private fun showReadyNotification() {
        val remoteView = RemoteViews(packageName, R.layout.notification_ready)
        remoteView.setTextViewText(R.id.exerciseName, exerciseName)
        val clickIntent = Intent(this, SplashActivity::class.java)
        clickIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        val builder = NotificationCompat.Builder(this, NotificationService.READY_CHANNEL)
            .setSmallIcon(R.drawable.ic_logo_24dp)
            .setContentText(getString(R.string.notification_rest_finished))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteView)
            .setUsesChronometer(true)
            .setWhen(startTime)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, getStartAppIntent(),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        getNotificationManager().notify(NotificationService.NOTIFICATION_READY_ID, builder.build())
    }

    private fun getStartAppIntent() : Intent {
        val startAppIntent = Intent(this, SplashActivity::class.java)
        startAppIntent.action = Intent.ACTION_MAIN
        startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return startAppIntent
    }

    private fun getNotificationManager() : NotificationManager {
        return getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
}