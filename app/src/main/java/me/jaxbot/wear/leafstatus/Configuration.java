package me.jaxbot.wear.leafstatus;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jonathan on 5/8/15.
 */
public class Configuration {
    public static int interval;
    public static boolean autoUpdate;
    public static boolean showPermanent;
    public static boolean useMetric;
    public static boolean noNightUpdates;
    public static boolean notifyOnlyWhenCharging;
    public static boolean alwaysShowStartHVAC;
    public static int defaultChargeLevel;

    public static void init(Context context) {
        SharedPreferences settings = context.getSharedPreferences("U", 0);
        SharedPreferences.Editor editor = settings.edit();

        showPermanent = settings.getBoolean("showPermanent", false);
        notifyOnlyWhenCharging = settings.getBoolean("notifyOnlyWhenCharging", false);
        alwaysShowStartHVAC = settings.getBoolean("alwaysShowStartHVAC", false);
        autoUpdate = settings.getBoolean("autoupdate", true);
        useMetric = settings.getBoolean("useMetric", false);
        noNightUpdates = settings.getBoolean("noNightUpdates", true);
        defaultChargeLevel = settings.getInt("defaultChargeLevel", 0);
        interval = settings.getInt("interval", 30);
    }

    public static void save(Context context) {
        SharedPreferences settings = context.getSharedPreferences("U", 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt("interval", interval);
        editor.putBoolean("autoupdate", autoUpdate);
        editor.putBoolean("showPermanent", showPermanent);
        editor.putBoolean("useMetric", useMetric);
        editor.putBoolean("noNightUpdates", noNightUpdates);
        editor.putBoolean("notifyOnlyWhenCharging", notifyOnlyWhenCharging);
        editor.putBoolean("alwaysShowStartHVAC", alwaysShowStartHVAC);
        editor.putInt("defaultChargeLevel", defaultChargeLevel);

        editor.commit();
    }
}
