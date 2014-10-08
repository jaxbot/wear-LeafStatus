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
        final Context context = this;

        // if the request was sent by the user, hide the controls
        if (intent.getBooleanExtra("hideControls", false))
            LeafNotification.sendNotification(context, carwings, false);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "Calling carwings update...");

                if (carwings.update()) {
                    Log.d(TAG, "Update completed, sending notification.");
                    LeafNotification.sendNotification(context, carwings);
                } else {
                    Log.d(TAG, "Update failed with an exception.");
                }
                return null;
            }
        }.execute(null, null, null);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
