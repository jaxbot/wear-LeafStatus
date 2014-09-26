package me.jaxbot.wear.leafstatus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class UpdateCarwingsService extends Service {
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public static final int NOTIFICATION_ID = 1;
    public static final String TAG = "UpdateCarwingsService";

    public UpdateCarwingsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Intent created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Intent started");

        final Carwings carwings = new Carwings(this);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "Calling carwings update...");

                if (carwings.update()) {

                    Log.d(TAG, "Update completed, sending notification.");
                    sendNotification(carwings.currentBattery, carwings.chargeTime);
                } else {
                    Log.d(TAG, "Update failed with an exception.");
                    sendNotification(1, "An exception occurred.");
                }
                return null;
            }
        }.execute(null, null, null);

        return START_STICKY;
    }

    private void sendNotification(int bars, String chargeTime) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MyActivity.class), 0);

        Intent acIntent = new Intent(this, StartAC.class);
        PendingIntent pendingIntentAC = PendingIntent.getBroadcast(this, 0, acIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String percent = String.valueOf(((bars * 10) / 12) * 10);
        String msg = chargeTime;

        Notification.Builder mBuilder =
            new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_leaf_notification)
                .setContentTitle("Leaf: " + percent + "%")
                .setStyle(new Notification.BigTextStyle()
                    .bigText(msg))
                .setContentText(msg)
                .addAction(R.drawable.ic_fan, "Start AC", pendingIntentAC);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
