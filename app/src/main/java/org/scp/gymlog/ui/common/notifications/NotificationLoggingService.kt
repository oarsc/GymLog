package org.scp.gymlog.ui.common.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import org.scp.gymlog.util.DateUtils.diff
import org.scp.gymlog.util.DateUtils.diffSeconds
import org.scp.gymlog.util.DateUtils.isPast
import org.scp.gymlog.util.DateUtils.timeInMillis
import org.scp.gymlog.util.DateUtils.toLocalDateTime
import java.time.LocalDateTime

class NotificationLoggingService : Service() {

    companion object {
        const val ACTION_STOP = "stop"
        const val ACTION_REPLACE = "replace"
        const val ACTION_START = "start"

        private val mBinder: IBinder = Binder()
    }

    private var countdownNotification: CountdownNotification? = null

    private var thread: Thread? = null

    private var startTime: Long = 0
    private lateinit var endDate: LocalDateTime
    private var exerciseName: String = ""
    private var variationId: Int = -1

    private var running = false
    private var scheduledIntent: PendingIntent? = null

    private val alarmManager
        by lazy { getSystemService(ALARM_SERVICE) as AlarmManager }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return super.onStartCommand(null, flags, startId)

        return when (intent.action) {
            ACTION_STOP -> {
                cancelThread()
                cancelScheduledNotification()

                ReadyNotification.close(this)
                CountdownNotification.close(this)
                stopSelfResult(startId)
                START_NOT_STICKY
            }
            ACTION_REPLACE -> {
                val milliseconds = intent.getLongExtra("milliseconds", 0)

                if (milliseconds > 0) {
                    endDate = milliseconds.toLocalDateTime

                    if (!endDate.isPast) {
                        if (running) {
                            countdownNotification?.apply {
                                remainingTime = endDate.diff().toInt()
                                maxTime = (endDate.timeInMillis - startTime).toInt()
                            }

                        } else {
                            val seconds = endDate.diffSeconds(startTime)
                            showCountdownNotification(seconds)
                            cancelThread()
                            thread = RefresherThread().also(Thread::start)
                        }

                        scheduleNotification(milliseconds)
                    } else {

                        cancelScheduledNotification()
                        cancelThread()
                    }
                }

                super.onStartCommand(intent, flags, startId)
            }
            else -> {
                exerciseName = intent.getStringExtra("name") ?: ""
                variationId = intent.getIntExtra("variationId", -1)
                val milliseconds = intent.getLongExtra("milliseconds", 0)
                val seconds = intent.getIntExtra("seconds", 10)

                endDate = milliseconds.toLocalDateTime
                startTime = endDate
                    .minusSeconds(seconds.toLong())
                    .timeInMillis

                showCountdownNotification(seconds)

                cancelThread()
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
                    countdownNotification?.apply {
                        remainingTime = endDate.diff().toInt()
                        update()
                    }

                    sleep(500)
                }
                endedNaturally = true
            } catch (e: InterruptedException) {
                // Interrupted
            } finally {
                if (endedNaturally) {
                    countdownNotification?.apply {
                        close()
                        countdownNotification = null
                    }
                    ReadyNotification(
                        this@NotificationLoggingService,
                        exerciseName,
                        startTime,
                        variationId
                    ).apply { showNotification() }
                    cancelScheduledNotification()
                }
                running = false
            }
        }
    }

    override fun onBind(intent: Intent) = mBinder

    override fun onDestroy() {
        cancelThread()
        cancelScheduledNotification()
        super.onDestroy()
    }

    private fun showCountdownNotification(seconds: Int) {
        ReadyNotification.close(this)

        countdownNotification = CountdownNotification(
            this,
            exerciseName,
            startTime,
            variationId
        ).apply {
            maxTime = seconds * 1000
            showNotification()
        }
    }

    private fun cancelThread() {
        thread?.apply {
            interrupt()
            thread = null
        }
    }

    private fun cancelScheduledNotification() {
        scheduledIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            scheduledIntent = null
        }
    }

    private fun scheduleNotification(endTime: Long) {
        cancelScheduledNotification()
        val intent = Intent(this, ScheduledNotificationBroadcastReceiver::class.java)

        intent.putExtra("exerciseName", exerciseName)
        intent.putExtra("startTime", startTime)
        intent.putExtra("variationId", variationId)
        intent.flags = intent.flags or Intent.FLAG_RECEIVER_FOREGROUND

        PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        ).also { pendingIntent ->
            scheduledIntent = pendingIntent
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, pendingIntent)
        }
    }

    class ScheduledNotificationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val notification = ReadyNotification(
                context.applicationContext,
                intent.getStringExtra("exerciseName") ?: "",
                intent.getLongExtra("startTime", 0),
                intent.getIntExtra("variationId", -1)
            )

            notification.showNotification()
        }
    }
}