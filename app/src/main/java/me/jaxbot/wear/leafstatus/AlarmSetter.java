package me.jaxbot.wear.leafstatus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by jonathan on 9/27/14.
 */
public class AlarmSetter {
    public static void setAlarm(Context context)
    {
        SharedPreferences settings = context.getSharedPreferences("U", 0);

        if (!settings.getBoolean("autoupdate", true)) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, 100, (settings.getInt("interval", 0) + 15) * 60 * 1000, sender);

    }

    public static void cancelAlarm(Context context)
    {
        SharedPreferences settings = context.getSharedPreferences("U", 0);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }
}
