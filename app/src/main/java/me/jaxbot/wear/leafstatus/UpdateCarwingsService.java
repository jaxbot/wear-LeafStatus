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

import java.util.Calendar;

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
        if (intent != null && intent.getBooleanExtra("hideControls", false))
            LeafNotification.sendNotification(context, carwings, false);
        else {
            // prevent any update leaks
            if (!carwings.autoUpdate) return 0;

            // if noNightUpdates is set, do not update between 8pm and 5am
            if (carwings.noNightUpdates) {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                Log.d(TAG, "Current hour is " + hour);
                if (hour > 19 || hour < 5) {
                    // Set the timer to activate close to 6am
                    // This is important, as updates could be delayed up to 314 minutes
                    // after 6am if this is not reset
                    int newTime;
                    if (hour > 12)
                        newTime = 5 - (hour - 24);
                    else
                        newTime = 5 - hour;

                    AlarmSetter.setAlarmTemp(this, System.currentTimeMillis() + newTime * 1000 * 60 * 60);
                    return START_STICKY;
                }
                Log.d(TAG, "Updating anyway.");
            }
        }


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "Calling carwings update...");

                if (carwings.update()) {
                    Log.d(TAG, "Update completed, sending notification.");
                    LeafNotification.sendNotification(context, carwings);
                    sendBroadcast(new Intent("leafstatus.update"));
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
