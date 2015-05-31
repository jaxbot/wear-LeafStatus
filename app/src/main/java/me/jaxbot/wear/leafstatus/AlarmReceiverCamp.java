package me.jaxbot.wear.leafstatus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by jonathan on 5/16/15.
 */
public class AlarmReceiverCamp extends WakefulBroadcastReceiver {
    static String TAG = "AlarmReceiverCamp";

    public AlarmReceiverCamp() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Alarm triggered, starting AC...");

        Intent acintent = new Intent(context, StartACService.class);
        acintent.putExtra("desiredState", true);
        context.startService(acintent);
    }
}
