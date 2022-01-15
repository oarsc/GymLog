package org.scp.gymlog.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import org.scp.gymlog.R;
import org.scp.gymlog.ui.common.notifications.NotificationLoggingService;

import java.util.Calendar;

public class NotificationService {
    public static final String COUNTDOWN_CHANNEL = "countdown";
    public static final String READY_CHANNEL = "ready";
    public static final int NOTIFICATION_COUNTDOWN_ID = 1;
    public static final int NOTIFICATION_READY_ID = 2;

    private final Context context;

    private static Calendar lastEndTime;

    public NotificationService(Context context) {
        this.context = context;
    }

    public static Calendar getLastEndTime() {
        return lastEndTime;
    }

    public void showNotification(Calendar endTime, int seconds, String exerciseName) {
        if (Calendar.getInstance().compareTo(endTime) > 0)
            return;

        lastEndTime = endTime;
        Intent intent = new Intent(context, NotificationLoggingService.class);
        intent.putExtra("seconds", seconds);
        intent.putExtra("milliseconds", endTime.getTimeInMillis());
        intent.putExtra("name", exerciseName);
        context.stopService(intent);
        context.startService(intent);
    }

    public void hideNotification() {
        Intent intent = new Intent(context, NotificationLoggingService.class);
        intent.setAction(NotificationLoggingService.ACTION_STOP);
        context.startService(intent);
        lastEndTime = null;
    }

    public void createNotificationsChannel() {
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        NotificationChannel countdownChannel = new NotificationChannel(COUNTDOWN_CHANNEL,
                context.getString(R.string.notification_title_countdown),
                NotificationManager.IMPORTANCE_DEFAULT);
        countdownChannel.setSound(null, null);
        notificationManager.createNotificationChannel(countdownChannel);

        NotificationChannel readyChannel = new NotificationChannel(READY_CHANNEL,
                context.getString(R.string.notification_title_ready),
                NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(readyChannel);
    }
}
