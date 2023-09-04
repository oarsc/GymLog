package org.scp.gymlog.ui.common.notifications

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import org.scp.gymlog.R
import org.scp.gymlog.SplashActivity
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.util.DateUtils.diff
import org.scp.gymlog.util.DateUtils.diffSeconds
import org.scp.gymlog.util.DateUtils.isPast
import org.scp.gymlog.util.DateUtils.timeInMillis
import org.scp.gymlog.util.DateUtils.toLocalDateTime
import java.time.LocalDateTime
import kotlin.math.roundToInt


class NotificationLoggingService : Service() {

    companion object {
        const val ACTION_STOP = "stop"
        const val ACTION_REPLACE = "replace"
        const val ACTION_START = "start"

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

    private lateinit var countdownRemoteView: RemoteViews
    private var foregroundNotification: Notification? = null

    private var thread: Thread? = null

    private var startTime: Long = 0
    private lateinit var endDate: LocalDateTime
    private var exerciseName: String? = null
    private var variationId: Int = -1

    private var running = false

    private var scheduledIntent: PendingIntent? = null

    private val notificationManager
        by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    private val alarmManager
        by lazy { getSystemService(ALARM_SERVICE) as AlarmManager }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return super.onStartCommand(null, flags, startId)

        return when (intent.action) {
            ACTION_STOP -> {
                scheduledIntent?.let {
                    alarmManager.cancel(it)
                    it.cancel()
                    scheduledIntent = null
                }
                notificationManager.cancel(NotificationService.NOTIFICATION_READY_ID)
                stopForeground(true)
                stopSelfResult(startId)
                START_NOT_STICKY
            }
            ACTION_REPLACE -> {
                val milliseconds = intent.getLongExtra("milliseconds", 0)

                if (milliseconds > 0) {
                    endDate = milliseconds.toLocalDateTime

                    if (running) {
                        if (!endDate.isPast) {
                            val diff = endDate.diff().toInt()
                            updateCountdownRemainingSeconds(diff)
                            updateCountdownMaxSeconds((endDate.timeInMillis - startTime).toInt())
                        }
                    } else {
                        if (!endDate.isPast) {
                            val seconds = endDate.diffSeconds(startTime)
                            showCountdownNotification(seconds)
                            thread?.interrupt()
                            thread = RefresherThread().also(Thread::start)
                        }
                    }
                    scheduleNotification(milliseconds)
                }

                super.onStartCommand(intent, flags, startId)
            }
            else -> {
                notificationManager.cancel(NotificationService.NOTIFICATION_READY_ID)
                exerciseName = intent.getStringExtra("name")
                variationId = intent.getIntExtra("variationId", -1)
                val milliseconds = intent.getLongExtra("milliseconds", 0)
                val seconds = intent.getIntExtra("seconds", 10)

                endDate = milliseconds.toLocalDateTime
                startTime = endDate
                    .minusSeconds(seconds.toLong())
                    .timeInMillis

                showCountdownNotification(seconds)
                thread?.interrupt()
                thread = RefresherThread().also(Thread::start)

                scheduleNotification(milliseconds)

                super.onStartCommand(intent, flags, startId)
            }
        }
    }

    inner class RefresherThread: Thread() {
        override fun run() {
            running = true
            var endedNaturally = false
            try {
                while (!endDate.isPast) {
                    val diff = endDate.diff().toInt()
                    updateNotification(diff)
                    sleep(500)
                }
                endedNaturally = true
            } catch (e: InterruptedException) {
                // Interrupted
            } finally {
                stopForeground(true)
                if (!endedNaturally) {
                    stopSelf()
                }
                running = false
            }
        }

        private fun updateNotification(remainingSeconds: Int) {
            updateCountdownRemainingSeconds(remainingSeconds)
            notificationManager.notify(NotificationService.NOTIFICATION_COUNTDOWN_ID, foregroundNotification)
        }
    }

    private val mBinder: IBinder = Binder()
    override fun onBind(intent: Intent) = mBinder

    override fun onDestroy() {
        thread?.apply {
            interrupt()
            thread = null
        }
        /*
        scheduledIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            scheduledIntent = null
        }
        */
        super.onDestroy()
    }

    private fun updateCountdownRemainingSeconds(remainingSeconds: Int) {
        countdownRemoteView.setInt(R.id.progressBar, "setProgress", remainingSeconds)
        countdownRemoteView.setTextViewText(R.id.seconds, (remainingSeconds/1000.0).roundToInt().toString())
    }

    private fun updateCountdownMaxSeconds(maxMilliseconds: Int) {
        countdownRemoteView.setInt(R.id.progressBar, "setMax", maxMilliseconds)
    }

    private fun showCountdownNotification(maxSeconds: Int) {
        countdownRemoteView = RemoteViews(packageName, R.layout.notification_countdown)
        countdownRemoteView.setInt(R.id.progressBar, "setMax", maxSeconds * 1000)
        countdownRemoteView.setTextViewText(R.id.exerciseName, exerciseName)
        val notification = generateCountdownNotification().also { this.foregroundNotification = it }

        notificationManager.cancel(NotificationService.NOTIFICATION_READY_ID)
        //notificationManager.notify(NotificationService.NOTIFICATION_COUNTDOWN_ID, notification)
        startForeground(NotificationService.NOTIFICATION_COUNTDOWN_ID, notification)
    }


    private fun generateCountdownNotification(): Notification {
        return NotificationCompat.Builder(this, NotificationService.COUNTDOWN_CHANNEL)
            .setSmallIcon(R.drawable.ic_logo_24dp)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(countdownRemoteView)
            .setOngoing(true)
            .setUsesChronometer(true)
            .setWhen(startTime)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, getStartAppIntent(this, variationId),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()
    }

    private fun scheduleNotification(endTime: Long) {
        scheduledIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            scheduledIntent = null
        }

        val intent = Intent(this, ScheduledNotification::class.java)

        intent.putExtra("exerciseName", exerciseName);
        intent.putExtra("startTime", startTime);
        intent.putExtra("variationId", variationId);

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        ).also { scheduledIntent = it }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, pendingIntent)
    }

    class ScheduledNotification : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val notification = generateReadyNotification(
                context.applicationContext,
                intent.getStringExtra("exerciseName") ?: "",
                intent.getLongExtra("startTime", 0),
                intent.getIntExtra("variationId", -1))

            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            //stopForeground(true)
            notificationManager.notify(NotificationService.NOTIFICATION_READY_ID, notification)
        }

        private fun generateReadyNotification(context: Context, exerciseName: String, startTime: Long, variationId: Int): Notification {
            val remoteView = RemoteViews(context.packageName, R.layout.notification_ready)
            remoteView.setTextViewText(R.id.exerciseName, exerciseName)

            return NotificationCompat.Builder(context.applicationContext, NotificationService.READY_CHANNEL)
                .setSmallIcon(R.drawable.ic_logo_24dp)
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
    }
}