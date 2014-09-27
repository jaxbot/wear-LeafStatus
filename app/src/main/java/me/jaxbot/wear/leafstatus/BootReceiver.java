package me.jaxbot.wear.leafstatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    // Set the alarm again once the phone boots up
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmSetter.setAlarm(context);
    }
}
