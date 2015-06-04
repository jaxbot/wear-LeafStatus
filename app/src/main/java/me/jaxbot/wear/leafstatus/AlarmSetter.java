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
    public static void setAlarm(Context context) {
        SharedPreferences settings = context.getSharedPreferences("U", 0);

        if (!settings.getBoolean("autoupdate", true)) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, intent, 0);

        int interval = (settings.getInt("interval", 0) + 15) * 60 * 1000;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, sender);

    }

    public static void setAlarmTemp(Context context, long time) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        // Note the different requestCode
        // This allows us to run separate alarms for the schedule and the catch up
        PendingIntent sender = PendingIntent.getBroadcast(context, 3, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, time, sender);
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

    public static boolean setCampAlarm(Context context)
    {
        Carwings carwings = new Carwings(context);
        if (carwings.currentBattery < 3 && !carwings.charging) {
            return false;
        }

        Intent intent = new Intent(context, AlarmReceiverCamp.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 4, intent, 0);

        int interval = 12 * 60 * 1000; // 12 minutes
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, 0, interval, sender);

        return true;
    }

    public static void cancelCampAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmReceiverCamp.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 4, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }
}
