package me.jaxbot.wear.leafstatus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by jonathan on 9/27/14.
 */
public class LeafNotification {

    public static final int NOTIFICATION_ID = 1;

    public static void sendNotification(Context context, Carwings carwings) {
        sendNotification(context, carwings, true);
    }

    public static void sendNotification(Context context, Carwings carwings, boolean showACControls) {
        NotificationManager mNotificationManager = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
            new Intent(context, MyActivity.class), 0);

        Intent acIntent = new Intent(context, StartAC.class);
        acIntent.putExtra("desiredState", !carwings.currentHvac);
        PendingIntent pendingIntentAC = PendingIntent.getBroadcast(context, 0, acIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent updateIntent = new Intent(context, UpdateCarwingsService.class);
        updateIntent.putExtra("hideControls", true);
        PendingIntent pendingCarwingsIntent = PendingIntent.getService(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String acText = carwings.currentHvac ? "Stop HVAC" : "Start HVAC";

        String msg;
        if (carwings.charging)
            msg = "Charging, " + carwings.chargeTime + "till charged [" + carwings.chargerType + "]";
        else
            msg = carwings.chargeTime + "to charge [" + carwings.chargerType + "]";

        Notification.Builder mBuilder =
            new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_leaf_notification)
                .setContentTitle("Leaf: " + carwings.currentBattery + " of 12, " + carwings.range)
                .setStyle(new Notification.BigTextStyle()
                    .bigText(msg))
                .setContentText(msg);

        if (showACControls) {
            mBuilder.addAction(R.drawable.ic_fan, acText, pendingIntentAC);
            if (!carwings.autoUpdate)
                mBuilder.addAction(R.drawable.ic_refresh, "Update", pendingCarwingsIntent);
        }
        else {
            mBuilder.addAction(R.drawable.ic_leaf_notification, "Sending command...", null);
            mBuilder.setProgress(0, 0, true);
        }

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setOngoing(carwings.showPermanent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


}
