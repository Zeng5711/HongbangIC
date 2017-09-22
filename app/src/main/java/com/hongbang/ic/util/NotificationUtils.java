package com.hongbang.ic.util;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.hongbang.ic.R;

import org.xutils.x;

@SuppressLint("NewApi")
public class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getName();

    public static void sendNotification(Context context, String title, String message) {
        sendNotification(context, null, title, message);
    }

    public static void sendNotification(Context context, String title, String message,
                                        boolean autoCancel) {
        sendNotification(context, null, title, message, autoCancel, true, true, true);
    }

    public static void sendNotification(Context context, Intent intent, String title, String message) {
        sendNotification(context, intent, title, message, false);
    }

    public static void sendNotification(Context context, Intent intent,
                                        String title, String message, boolean autoCancel) {
        sendNotification(context, intent, title, message, autoCancel, true, true,
                true);
    }

    public static void sendNotification(final Context context, Intent intent, String title,
                                        String message, boolean autoCancel, boolean isSound,
                                        boolean isVibrate, boolean isLights) {
        Notification notify;
        int version = android.os.Build.VERSION.SDK_INT;
        if (version >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context);
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
            builder.setAutoCancel(true);
            builder.setContentTitle(title);
            builder.setContentText(message);
            builder.setWhen(System.currentTimeMillis());
            int defaults = 0;
            if (isSound) {
                defaults |= Notification.DEFAULT_SOUND;
            }
            if (isVibrate) {
                defaults |= Notification.DEFAULT_VIBRATE;
            }
            if (isLights) {
                defaults |= Notification.DEFAULT_LIGHTS;
            }
            if (intent != null) {
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                        intent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(pendingIntent);
            }
            builder.setDefaults(defaults);
            notify = builder.build();
        } else {
            Builder builder = new Builder(context);
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
            builder.setContentTitle(title);
            builder.setContentText(message);
            builder.setAutoCancel(true);
            builder.setWhen(System.currentTimeMillis());
            int defaults = 0;
            if (isSound) {
                defaults |= Notification.DEFAULT_SOUND;
            }
            if (isVibrate) {
                defaults |= Notification.DEFAULT_VIBRATE;
            }
            if (isLights) {
                defaults |= Notification.DEFAULT_LIGHTS;
            }

            if (intent != null) {
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                        intent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(pendingIntent);
            }
            builder.setDefaults(defaults);
            notify = builder.build();
        }
        notify.flags = notify.flags | Notification.FLAG_AUTO_CANCEL;

        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        final int id = (int) (System.currentTimeMillis() % 1000000);
        manager.notify(id, notify);
        if (autoCancel) {
            x.task().postDelayed(new Runnable() {
                @Override
                public void run() {
                    clearNotification(context, id);
                }
            }, 15 * 1000);
        }
    }

    public static void clearNotification(Context context, int id) {
        if (context == null) {
            return;
        }
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(id);
    }

    public static void clearAllNotifications(Context context) {
        if (context == null) {
            return;
        }
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

}
