package me.jaxbot.wear.leafstatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jonathan on 9/21/14.
 */
public class StartAC extends BroadcastReceiver {
    final static String TAG = "StartAC";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Starting AC...");

        Intent acintent = new Intent(context, StartACService.class);
        context.startService(acintent);
    }
}
