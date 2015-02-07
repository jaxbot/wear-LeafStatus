package me.jaxbot.wear.leafstatus;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class StartACService extends Service {
    final static String TAG = "StartACService";

    public StartACService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting AC in service...");

        final boolean state = intent.getBooleanExtra("desiredState", false);

        Log.i(TAG, "Desired: " + String.valueOf(state));

        final Context context = this;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Carwings carwings = new Carwings(context);

                carwings.currentHvac = state;
                LeafNotification.sendNotification(context, carwings, false);

                boolean success = false;
                for (int i = 0; i < 3; i++) {
                    Log.i(TAG, "Attempt " + i + " to start AC...");
                    if (carwings.startAC(state)) {
                        success = true;
                        Log.i(TAG, "AC started.");
                        break;
                    } else {
                        Log.i(TAG, "StartAC failed, likely due to login.");
                    }
                }

                if (!success)
                    carwings.currentHvac = !state;

                LeafNotification.sendNotification(context, carwings);

                stopSelf();

                return null;
            }
        }.execute(null, null, null);
        return START_STICKY;
    }
}
