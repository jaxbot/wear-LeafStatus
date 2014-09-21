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
        Log.d(TAG, "Intent started!");

        SharedPreferences settings = getSharedPreferences("U", 0);
        final Carwings carwings = new Carwings(settings.getString("username", ""), settings.getString("password", ""));

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "Calling carwings update...");

                carwings.update();

                Log.d(TAG, "Update completed, sending notification.");
                sendNotification(carwings.currentBattery, carwings.chargeTime);
                return null;
            }
        }.execute(null, null, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Hello", "Sticky!");

        return START_STICKY;
    }

    private void sendNotification(int bars, String chargeTime) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MyActivity.class), 0);

        // Build an intent for an action to view a map
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        Uri geoUri = Uri.parse("geo:0,0?q=" + Uri.encode("house"));
        mapIntent.setData(geoUri);
        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(this, 0, mapIntent, 0);

        String percent = String.valueOf(((bars * 10) / 12) * 10);
        String msg = chargeTime;

        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.abc_ab_bottom_solid_dark_holo)
                        .setContentTitle("Leaf: " + percent + "%")
                        .setStyle(new Notification.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .addAction(R.drawable.ic_launcher, "Start AC", mapPendingIntent);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
