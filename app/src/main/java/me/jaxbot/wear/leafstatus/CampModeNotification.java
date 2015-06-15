package me.jaxbot.wear.leafstatus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jonathan on 6/7/15.
 */
public class CampModeNotification {
    public static final int NOTIFICATION_ID = 2;

    public static void showNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.ic_fan)
                        .setContentTitle("Camp mode on");

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MyActivity.class), 0);

        Intent acIntent = new Intent(context, StopCampMode.class);
        PendingIntent pendingIntentAC = PendingIntent.getBroadcast(context, 0, acIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.addAction(R.drawable.ic_fan, "Stop camp mode", pendingIntentAC);

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setOngoing(true);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static void hideNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
}
