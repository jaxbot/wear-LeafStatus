package me.jaxbot.wear.leafstatus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MyActivity extends ActionBarActivity {

    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        sendNotification("Time till full: 2:30");
    }
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MyActivity.class), 0);

        // Build an intent for an action to view a map
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        Uri geoUri = Uri.parse("geo:0,0?q=" + Uri.encode("house"));
        mapIntent.setData(geoUri);
        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(this, 0, mapIntent, 0);

        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.abc_ab_bottom_solid_dark_holo)
                        .setContentTitle("Leaf: 50%")
                        .setStyle(new Notification.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .addAction(R.drawable.ic_launcher, "Start AC", mapPendingIntent);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
