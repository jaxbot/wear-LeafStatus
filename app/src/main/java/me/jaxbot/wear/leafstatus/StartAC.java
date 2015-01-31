package me.jaxbot.wear.leafstatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by jonathan on 9/21/14.
 */
public class StartAC extends BroadcastReceiver {
    final static String TAG = "StartAC";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Starting AC...");

        final boolean state = intent.getBooleanExtra("desiredState", false);

        Log.i(TAG, "Desired: " + String.valueOf(state));

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

                return null;
            }
        }.execute(null, null, null);

    }
}
