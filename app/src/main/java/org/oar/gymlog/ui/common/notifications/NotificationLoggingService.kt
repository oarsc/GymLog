package org.oar.gymlog.ui.common.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import org.oar.gymlog.util.DateUtils.diff
import org.oar.gymlog.util.DateUtils.diffSeconds
import org.oar.gymlog.util.DateUtils.isSystemTimePast

class NotificationLoggingService : Service() {

    companion object {
        const val ACTION_REPLACE = "replace"
        const val ACTION_START = "start"
        const val ACTION_SCHEDULED = "scheduled"

        private val mBinder: IBinder = Binder()
    }

    private var countdownNotification: CountdownNotification? = null

    private var thread: Thread? = null

    private var startTime: Long = 0
    private var endDate: Long = 0
    private var exerciseName: String = ""
    private var variationId: Int = -1

    private var running = false
    private var scheduledIntent: PendingIntent? = null
    private var lastNotificationId = 0
    private var nextNotificationId = 0

    private val alarmManager
        by lazy { getSystemService(ALARM_SERVICE) as AlarmManager }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return super.onStartCommand(null, flags, startId)

        return when (intent.action) {
            ACTION_SCHEDULED -> {
                val endTime = intent.getLongExtra("endTime", Long.MIN_VALUE)

                if ((System.currentTimeMillis() + 1000) < endTime) {
                    scheduleNotification(endTime)
                    START_STICKY
                } else {
                    CountdownNotification.close(this)
                    countdownNotification = null
                    showReadyNotification()
                    START_NOT_STICKY
                }
            }
            ACTION_REPLACE -> {
                val milliseconds = intent.getLongExtra("milliseconds", 0)

                if (milliseconds > 0) {
                    endDate = milliseconds

                    if (!endDate.isSystemTimePast) {
                        if (running) {
                            countdownNotification?.apply {
                                remainingTime = endDate.diff().toInt()
                                maxTime = (endDate - startTime).toInt()
                            }

                        } else {
                            val seconds = endDate.diffSeconds(startTime)
                            showCountdownNotification(seconds)
                            cancelThread()
                            thread = RefresherThread().also(Thread::start)
                        }

                        nextNotificationId++
                        scheduleNotification(milliseconds)
                    } else {

                        cancelScheduledNotification()
                        cancelThread()
                    }
                }

                START_STICKY
            }
            else -> {
                exerciseName = intent.getStringExtra("name") ?: ""
                variationId = intent.getIntExtra("variationId", -1)
                val milliseconds = intent.getLongExtra("milliseconds", 0)
                val seconds = intent.getIntExtra("seconds", 10)

                endDate = milliseconds
                startTime = endDate - (seconds * 1000L)

                showCountdownNotification(seconds)

                cancelThread()
                thread = RefresherThread().also(Thread::start)

                nextNotificationId++
                scheduleNotification(milliseconds)

                START_STICKY
            }
        }

    }

    inner class RefresherThread: Thread() {
        override fun run() {
            running = true
            try {
                while (!endDate.isSystemTimePast) {
                    countdownNotification?.apply {
                        remainingTime = endDate.diff().toInt()
                        update()
                    }

                    sleep(500)
                }

                countdownNotification?.apply {
                    close()
                    countdownNotification = null
                }
                showReadyNotification()
                cancelScheduledNotification()

            } catch (e: InterruptedException) {
                // Interrupted
            } finally {
                running = false
            }
        }
    }

    override fun onBind(intent: Intent) = mBinder

    override fun onDestroy() {
        cancelThread()
        cancelScheduledNotification()
        ReadyNotification.close(this)
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

        if (alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(this, NotificationLoggingService::class.java).apply {
                action = ACTION_SCHEDULED
                flags = flags or Intent.FLAG_RECEIVER_FOREGROUND
                putExtra("exerciseName", exerciseName)
                putExtra("startTime", startTime)
                putExtra("endTime", endTime)
                putExtra("variationId", variationId)
            }

            scheduledIntent = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, scheduledIntent!!)
        }
    }

    private fun showReadyNotification() {
        if (lastNotificationId != nextNotificationId) {
            lastNotificationId = nextNotificationId

            ReadyNotification(
                this@NotificationLoggingService,
                exerciseName,
                startTime,
                variationId
            ).apply { showNotification() }
        }
    }
}