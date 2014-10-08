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

    public static void sendNotification(Context context, int bars, boolean currentHvacState, String chargeTime, String range) {
        sendNotification(context, bars, currentHvacState, chargeTime, range, true);
    }

    public static void sendNotification(Context context, int bars, boolean currentHvacState, String chargeTime, String range, boolean showACControls) {
        NotificationManager mNotificationManager = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
            new Intent(context, MyActivity.class), 0);

        Intent acIntent = new Intent(context, StartAC.class);
        acIntent.putExtra("desiredState", !currentHvacState);
        PendingIntent pendingIntentAC = PendingIntent.getBroadcast(context, 0, acIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String acText = currentHvacState ? "Stop HVAC" : "Start HVAC";

        String msg = chargeTime + "till charged";

        Notification.Builder mBuilder =
            new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_leaf_notification)
                .setContentTitle("Leaf: " + bars + " / 12, " + range)
                .setStyle(new Notification.BigTextStyle()
                    .bigText(msg))
                .setContentText(msg);

        if (showACControls)
            mBuilder.addAction(R.drawable.ic_fan, acText, pendingIntentAC);
        else
            mBuilder.addAction(R.drawable.ic_fan, "Sending AC command...", null);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


}
