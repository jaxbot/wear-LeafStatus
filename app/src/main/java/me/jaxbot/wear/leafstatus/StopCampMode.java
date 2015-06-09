package me.jaxbot.wear.leafstatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StopCampMode extends BroadcastReceiver {
    final static String TAG = "StopCampMode";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Stopping camp mode...");

        AlarmSetter.cancelCampAlarm(context);
        CampModeNotification.hideNotification(context);
    }
}
