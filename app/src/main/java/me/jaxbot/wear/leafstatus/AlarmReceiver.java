package me.jaxbot.wear.leafstatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class AlarmReceiver extends WakefulBroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Hello", "Alarming!");
        Intent service = new Intent(context, UpdateCarwingsService.class);
        startWakefulService(context, service);
    }
}
