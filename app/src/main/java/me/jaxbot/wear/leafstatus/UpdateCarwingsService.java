package me.jaxbot.wear.leafstatus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UpdateCarwingsService extends Service {
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public static final int NOTIFICATION_ID = 1;

    public UpdateCarwingsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Hello", "Intented!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Hello", "Sticky!");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                carwingsLogin();
                return null;
            }
        }.execute(null, null, null);

        return START_STICKY;
    }

    private void carwingsLogin() {
        // Create a new HttpClient and Post Header
        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost("https://www.nissanusa.com/owners/j_spring_security_check");

        SharedPreferences settings = getSharedPreferences("U", 0);

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("j_username", settings.getString("username", "")));
            nameValuePairs.add(new BasicNameValuePair("j_passwordHolder", "Password"));
            nameValuePairs.add(new BasicNameValuePair("j_password", settings.getString("password", "")));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            System.out.println(httpclient.getCookieStore().getCookies().toString());

            DefaultHttpClient httpclient2 = new DefaultHttpClient();
            httpclient2.setCookieStore(httpclient.getCookieStore());
            HttpGet httpget = new HttpGet("https://www.nissanusa.com/owners/vehicles/statusRefresh?id=50405");
            httpclient2.execute(httpget);

            httpclient2 = new DefaultHttpClient();
            httpclient2.setCookieStore(httpclient.getCookieStore());

            HttpGet httpgetdata = new HttpGet("https://www.nissanusa.com/owners/vehicles/pollStatusRefresh?id=50405");
            response = httpclient2.execute(httpgetdata);

            InputStream inputStream = response.getEntity().getContent();
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();

            System.out.println(response.toString());
            System.out.println(result);

            try {
                JSONObject jObject = new JSONObject(result);
                sendNotification(jObject.getInt("currentBattery"), jObject.getString("chargeTime"));
            } catch (Exception e) {
                System.out.println(e);
            }

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            System.out.println(e.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.toString());
        }
    }

    private void sendNotification(int bars, String chargeTime) {
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

        String percent = String.valueOf(((bars * 10) / 12) * 10);
        String msg = chargeTime;

        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.abc_ab_bottom_solid_dark_holo)
                        .setContentTitle("Leaf: " + percent + "%")
                        .setStyle(new Notification.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .addAction(R.drawable.ic_launcher, "Start AC", mapPendingIntent);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }



    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
