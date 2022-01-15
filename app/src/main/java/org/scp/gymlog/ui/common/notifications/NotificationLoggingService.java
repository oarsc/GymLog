package org.scp.gymlog.ui.common.notifications;

import static org.scp.gymlog.service.NotificationService.COUNTDOWN_CHANNEL;
import static org.scp.gymlog.service.NotificationService.NOTIFICATION_COUNTDOWN_ID;
import static org.scp.gymlog.service.NotificationService.NOTIFICATION_READY_ID;
import static org.scp.gymlog.service.NotificationService.READY_CHANNEL;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.scp.gymlog.R;
import org.scp.gymlog.SplashActivity;
import org.scp.gymlog.util.DateUtils;

import java.util.Calendar;

public class NotificationLoggingService extends Service {
    public static final String ACTION_STOP = "stop";

    private final IBinder mBinder = new Binder();
    private boolean end = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private RemoteViews countdownRemoteView;
    private Notification notification;
    private Thread thread;
    private long startTime;
    private String exerciseName;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || ACTION_STOP.equals(intent.getAction())) {
            stopSelfResult(startId);
            return START_NOT_STICKY;
        }

        getNotificationManager().cancel(NOTIFICATION_READY_ID);

        Calendar endDate = Calendar.getInstance();
        exerciseName = intent.getStringExtra("name");
        long milliseconds = intent.getLongExtra("milliseconds", 0);
        int seconds = intent.getIntExtra("seconds", 10);

        if (milliseconds > 0) {
            endDate.setTimeInMillis(milliseconds);
            Calendar startDate = (Calendar) endDate.clone();
            startDate.add(Calendar.SECOND, -seconds);
            startTime = startDate.getTimeInMillis();
        } else {
            startTime = endDate.getTimeInMillis();
            endDate.add(Calendar.SECOND, seconds);
        }

        if (seconds <= 0) {
            showReadyNotification();
            stopSelf();

        } else {
            startForeground(NOTIFICATION_COUNTDOWN_ID, notification = generateCountdownNotification(seconds));
            thread = new Thread(() -> {
                boolean endedNaturally = false;
                try {
                    int diff = (int) DateUtils.diff(Calendar.getInstance(), endDate);
                    do {
                        updateNotification(diff);
                        Thread.sleep(500);
                        diff = (int) DateUtils.diff(Calendar.getInstance(), endDate);
                    } while (diff > 0);
                    endedNaturally = true;

                } catch (InterruptedException e) {
                    // Interrupted


                } finally {
                    stopForeground(true);
                    if (endedNaturally) {
                        showReadyNotification();
                        stopSelf();
                    }
                }
            });
            thread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        super.onDestroy();
    }

    private Notification generateCountdownNotification(int maxSeconds) {
        countdownRemoteView = new RemoteViews(getPackageName(), R.layout.notification_countdown);

        countdownRemoteView.setInt(R.id.progressBar, "setMax", maxSeconds*1000);
        countdownRemoteView.setTextViewText(R.id.exerciseName, exerciseName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, COUNTDOWN_CHANNEL)
                .setSmallIcon(R.drawable.ic_logo_24dp)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(countdownRemoteView)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, getStartAppIntent(),
                        PendingIntent.FLAG_IMMUTABLE));

        return builder.build();
    }

    private void updateNotification(int remainingSeconds) {
        updateViewSeconds(remainingSeconds);
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_COUNTDOWN_ID, notification);
    }

    private void updateViewSeconds(int remainingSeconds) {
        countdownRemoteView.setInt(R.id.progressBar, "setProgress", remainingSeconds);
        countdownRemoteView.setTextViewText(R.id.seconds, String.valueOf(Math.round(remainingSeconds/1000.)));
    }

    private void showReadyNotification() {
        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.notification_ready);

        remoteView.setTextViewText(R.id.exerciseName, exerciseName);

        Intent clickIntent = new Intent(this, SplashActivity.class);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, READY_CHANNEL)
                .setSmallIcon(R.drawable.ic_logo_24dp)
                .setContentText(getString(R.string.notification_rest_finished))
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteView)
                .setUsesChronometer(true)
                .setWhen(startTime)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, getStartAppIntent(),
                        PendingIntent.FLAG_IMMUTABLE));

        getNotificationManager().notify(NOTIFICATION_READY_ID, builder.build());
    }

    private Intent getStartAppIntent() {
        Intent startAppIntent = new Intent(this, SplashActivity.class);
        startAppIntent.setAction(Intent.ACTION_MAIN);
        startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return startAppIntent;
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
