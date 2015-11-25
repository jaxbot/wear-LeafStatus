package me.jaxbot.wear.leafstatus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLEncoder;

/**
 * Created by jonathan on 9/21/14.
 */
public class Carwings {
    static String TAG = "Carwings";

    public static String[] PortalURL = {
        "https://owners.nissanusa.com/nowners/", // US
        "https://carwings.mynissan.ca/", // CA
        "http://www.nissan.co.uk/GB/en/YouPlus/welcome_pack_leaf.html/" // UK
    };

    private String username;
    private String password;
    private String vin;

    public int currentBattery;
    public String range;
    public String chargeTime;
    public boolean currentHvac;
    public String lastUpdateTime;
    public String chargerType;
    public boolean charging;
    public boolean autoUpdate;
    public boolean showPermanent;
    public boolean useMetric;
    public boolean noNightUpdates;
    public boolean notifyOnlyWhenCharging;
    public boolean alwaysShowStartHVAC;

    // Endpoint url for this instance
    String url;

    SharedPreferences settings;

    Context ctx;
    Carwings thisInstance = this;

    // Disgusting, but we're using the web frontend, and thus will pretend
    String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";

    public Carwings(Context context)
    {
        settings = context.getSharedPreferences("U", 0);
        this.username = settings.getString("username", "");
        this.password = settings.getString("password", "");
        this.vin = settings.getString("vin", "");
        this.currentBattery = settings.getInt("currentBattery", 0);
        this.chargeTime = settings.getString("chargeTime", "");
        this.range = settings.getString("range", "");
        this.lastUpdateTime = settings.getString("lastupdate", "");
        this.url = "https://gdcportalgw.its-mo.com/orchestration_1021/gdc/";
        this.chargerType = settings.getString("chargerType", "L1");
        this.charging = settings.getBoolean("charging", false);
        this.autoUpdate = settings.getBoolean("autoupdate", true);
        this.showPermanent = settings.getBoolean("showPermanent", false);
        this.useMetric = settings.getBoolean("useMetric", false);
        this.noNightUpdates = settings.getBoolean("noNightUpdates", true);
        this.notifyOnlyWhenCharging = settings.getBoolean("notifyOnlyWhenCharging", false);
        this.alwaysShowStartHVAC = settings.getBoolean("alwaysShowStartHVAC", false);
        Configuration.init(context);

        ctx = context;
    }
    private CookieStore login() {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        if (username.equals("")) return null;
        try {
            String result = getHTTPString(url + "/UserLoginRequest.php?UserId=" + URLEncoder.encode(username, "utf-8") + "&cartype=&tz=&lg=en-US&DCMID=&VIN=&RegionCode=NNA&Password=" + URLEncoder.encode(password, "utf-8"));
            JSONObject jObject = new JSONObject(result);
            String vin = jObject.getJSONObject("CustomerInfo").getJSONObject("VehicleInfo").getString("VIN");
            System.out.println("Your VIN is " + vin);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("vin", vin);
            editor.commit();

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return null;
    }

    public boolean trylogin() {
        return !getCarId(login()).equals("");
    }

    private String getCarId(CookieStore jar) {
        // Check if we have already grabbed this
        String cachedCarID = settings.getString("vin", "");
        if (!cachedCarID.equals(""))
            return cachedCarID;

        // This is a particularly bad and non-future-safe operation,
        // but so is the entire application, since Nissan's API is internal
        String vehicleHTML = getHTTPString(url + "user/home");
        System.out.println(vehicleHTML);
        Pattern pattern = Pattern.compile("(.*)var vinId = \"([a-zA-Z0-9]+)\"(.*)");
        Matcher m = pattern.matcher(vehicleHTML);
        if (m.matches()) {
            cachedCarID = m.group(2);

            // Save it, since it is unlikely to change
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("vin", cachedCarID);
            editor.commit();

            return cachedCarID;
        } else {
            Log.e(TAG, "Failed to find vehicle id");
            return "";
        }
    }

    public boolean update() {
        try {
            String encodedUsername = "";
            try {
               encodedUsername = URLEncoder.encode(username, "utf-8");
            } catch (Exception e) {
            }

            String request = getHTTPString(url + "BatteryStatusCheckRequest.php?UserId=" + encodedUsername + "&cartype=&VIN=" + vin + "&RegionCode=NNA&tz=America%2FNew_York&lg=en-US");
            JSONObject requestResults = new JSONObject(request);
            final String resultKey = requestResults.getString("resultKey");

            final Timer poller = new Timer();
            final String finalEncodedUsername = encodedUsername;
            poller.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("checking bat status");
                    try {
                        String request = getHTTPString(url + "BatteryStatusCheckResultRequest.php?UserId=" + URLEncoder.encode(username, "utf-8") + "&cartype=&resultKey=" + resultKey + "&tz=America%2FNew_York&lg=en-US&VIN=" + vin + "&RegionCode=NNA");
                        JSONObject requestResults = new JSONObject(request);
                        if (requestResults.has("chargeMode")) {
                            poller.cancel();

                            System.out.println("We got the data we need. It is: " + request);
                            String recordsRequest = getHTTPString(url + "BatteryStatusRecordsRequest.php?UserId=" + finalEncodedUsername + "&cartype=&tz=America%2FNew_York&lg=en-US&VIN=" + vin + "&RegionCode=NNA");
                            System.out.println("records: " + recordsRequest);
                            JSONObject records = new JSONObject(recordsRequest).getJSONObject("BatteryStatusRecords");

                            currentBattery = records.getJSONObject("BatteryStatus").getInt("BatteryRemainingAmount");

                            String l1Time = "null";
                            String l2Time = "null";
                            String l3Time = "null";

                            try {
                                l1Time = records.getJSONObject("TimeRequiredToFull").getString("HourRequiredToFull");
                            } catch (Exception e) {}
                            try {
                            l2Time = records.getJSONObject("TimeRequiredToFull200").getString("HourRequiredToFull");
                            } catch (Exception e) {}
                            try {
                                l3Time = records.getJSONObject("TimeRequiredToFull200_6kW").getString("HourRequiredToFull");
                            } catch (Exception e) {}

                            chargeTime = l1Time;
                            chargerType = "L1";

                            int defaultCharger = settings.getInt("defaultChargeLevel", 0);
                            Log.d(TAG, "def: " + defaultCharger);

                            if (chargeTime.equals("null") || (!l2Time.equals("null") && defaultCharger == 1)) {
                                chargeTime = l2Time;
                                chargerType = "L2";
                            }

                            if (chargeTime.equals("null") || (!l3Time.equals("null") && defaultCharger == 2)) {
                                chargeTime = l3Time;
                                chargerType = "L2+";
                            }

                            if (chargeTime.equals("null")) {
                                chargeTime = "Unknown";
                                chargerType = "?";
                            }

                            charging = !requestResults.getString("chargeMode").equals("NOT_CHARGING");

                            int range_km = (int)Float.parseFloat(records.getString("CruisingRangeAcOff")) / 1000;
                            System.out.println("rangekm: " + range_km);
                            if (useMetric)
                                range = Math.round(range_km) + " km";
                            else
                                range = Math.round(range_km * 0.621371) + " mi";

                            Time today = new Time(Time.getCurrentTimezone());
                            today.setToNow();
                            lastUpdateTime = today.format("%Y-%m-%d %H:%M:%S");

                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("range", range);
                            editor.putString("chargeTime", chargeTime);
                            editor.putBoolean("charging", charging);
                            editor.putString("chargerType", chargerType);
                            editor.putString("lastupdate", lastUpdateTime);
                            editor.putInt("currentBattery", currentBattery);
                            editor.commit();

                            Log.d(TAG, "Update completed, sending notification.");
                            LeafNotification.sendNotification(ctx, thisInstance);
                            ctx.sendBroadcast(new Intent("leafstatus.update"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }, 0, 10000);

            return true;
        } catch (Exception e) {
            System.out.println("Failure!!!");
            System.out.println(e);
        }

        return false;
    }

    public boolean startAC(boolean desired) {
        String endpoint = desired ? "ACRemoteRequest" : "ACRemoteOffRequest";

        try {
            String output = getHTTPString(url + endpoint + ".php?UserId=" + URLEncoder.encode(this.username, "utf-8") + "&cartype=&VIN=" + this.vin + "&RegionCode=NNA&tz=America%2FNew_York&lg=en-US");
            if (!output.contains("success")) {
                Log.e(TAG, "Start AC failed. Output: " + output);
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return true;
    }

    private String getHTTPString(String url) {
        System.out.println("Starting GET " + url);
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("User-Agent", UA);

            HttpResponse response = httpclient.execute(httpget);

            InputStream inputStream = response.getEntity().getContent();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String result = "";

            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }

            inputStream.close();

            System.out.println("Done GET " + url);
            return result;
        } catch (Exception e) {
            System.out.println(e);
        }

        return "";

    }

}
