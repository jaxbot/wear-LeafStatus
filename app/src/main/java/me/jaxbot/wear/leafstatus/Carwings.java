package me.jaxbot.wear.leafstatus;

import android.content.Context;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Disgusting, but we're using the web frontend, and thus will pretend
    String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";

    public Carwings(Context context)
    {
        settings = context.getSharedPreferences("U", 0);
        this.username = settings.getString("username", "");
        this.password = settings.getString("password", "");
        this.currentBattery = settings.getInt("currentBattery", 0);
        this.chargeTime = settings.getString("chargeTime", "");
        this.range = settings.getString("range", "");
        this.lastUpdateTime = settings.getString("lastupdate", "");
        this.url = PortalURL[settings.getInt("portal", 0)];
        this.chargerType = settings.getString("chargerType", "L1");
        this.charging = settings.getBoolean("charging", false);
        this.autoUpdate = settings.getBoolean("autoupdate", true);
        this.showPermanent = settings.getBoolean("showPermanent", false);
        this.useMetric = settings.getBoolean("useMetric", false);
        this.noNightUpdates = settings.getBoolean("noNightUpdates", true);
        this.notifyOnlyWhenCharging = settings.getBoolean("notifyOnlyWhenCharging", false);
        this.alwaysShowStartHVAC = settings.getBoolean("alwaysShowStartHVAC", false);
    }
    private CookieStore login() {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        if (username.equals("")) return null;
        try {
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("User-Agent", UA);
            httpclient.execute(httpget);

            CookieStore jar = httpclient.getCookieStore();
            httpclient.setCookieStore(jar);
            HttpPost httppost = new HttpPost(url + "user/login");
            httppost.setHeader("User-Agent", UA);

            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username", username));
            nameValuePairs.add(new BasicNameValuePair("password", password));
            nameValuePairs.add(new BasicNameValuePair("callFrom", ""));
            nameValuePairs.add(new BasicNameValuePair("status", ""));
            nameValuePairs.add(new BasicNameValuePair("_rememberMeCheckbox", ""));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            return httpclient.getCookieStore();
        } catch (ClientProtocolException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println(e.toString());
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
        String vehicleHTML = getHTTPString(url + "user/home", jar);
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
            CookieStore jar = this.login();
            String carid = this.getCarId(jar);

            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url + "EV/statusRefresh?vin=" + carid);
            httpget.setHeader("User-Agent", UA);
            httpclient.setCookieStore(jar);
            httpclient.execute(httpget);

            String result = getHTTPString(url + "EV/refresh?vin=" + carid, jar);

            // name="btryLvlNb" value="8"
            System.out.println("prebattt");
            System.out.println(result);
            Matcher matcher = Pattern.compile("(.*)name=\"btryLvlNb\" value=\"([a-zA-Z0-9 ]+)\"(.*)").matcher(result);
            if (matcher.matches()) {
                this.currentBattery = Integer.parseInt(matcher.group(2));
                System.out.println("bat: " + this.currentBattery);
            }

            /*
                <td class="chrgType">
                    Trickle
                </td>
                <td class="chrgTypeText">
                    7 hrs 0 min 
                </td>
            */
            matcher = Pattern.compile("(.*) class=\"chrgType\">[\\s]+Trickle[\\s]+\\</td\\>[\\s]+\\<td class=\"chrgTypeText\"\\>[\\s]+([a-zA-Z0-9\\ ]+)(.*)").matcher(result);
            matcher.matches();
            String l1Time = matcher.group(2);
            System.out.println("l1: " + l1Time);

            // <input type="hidden" name="chrgTm220KVTx" value="3 hrs 30 min " id="chrgTm220KVTx" />
            matcher = Pattern.compile("(.*) name=\"chrgTm220KVTx\" value=\"([a-zA-Z0-9\\ ]+)\"(.*)").matcher(result);
            matcher.matches();
            String l2Time = matcher.group(2);
            System.out.println("l2: " + l2Time);

            // <input type="hidden" name="rmngChrg220KvChrgrTx" value="2 hrs 30 min " id="rmngChrg220KvChrgrTx" />
            matcher = Pattern.compile("(.*) name=\"rmngChrg220KvChrgrTx\" value=\"([a-zA-Z0-9\\ ]+)\"(.*)").matcher(result);
            matcher.matches();
            String l3Time = matcher.group(2);
            System.out.println("l3: " + l3Time);

            // When the car is charging, only one of the ltimes will be populated
            // with a value other than null. Fall through if null, or use default
            // time if available
            this.chargeTime = l1Time;
            this.chargerType = "L1";

            int defaultCharger = settings.getInt("defaultChargeLevel", 0);
            Log.d(TAG, "def: " + defaultCharger);

            if (chargeTime.equals("null") || (!l2Time.equals("null") && defaultCharger == 1)) {
                this.chargeTime = l2Time;
                this.chargerType = "L2";
            }

            if (chargeTime.equals("null") || (!l3Time.equals("null") && defaultCharger == 2)) {
                this.chargeTime = l3Time;
                this.chargerType = "L2+";
            }

            if (chargeTime.equals("null")) {
                this.chargeTime = "Unknown";
                this.chargerType = "?";
            }

            // <input type="hidden" name="hvacIn" value="false" id="hvacIn" />
            matcher = Pattern.compile("(.*) name=\"hvacIn\" value=\"([a-zA-Z0-9\\ ]+)\"(.*)").matcher(result);
            this.currentHvac = !matcher.group(2).equals("false");
            System.out.println("chvac: " + this.currentHvac);

            // <input type="hidden" name="chargingStsCd" value="NOT_CHARGING" id="chargingStsCd" />
            matcher = Pattern.compile("(.*) name=\"chargingStsCd\" value=\"([a-zA-Z0-9\\ ]+)\"(.*)").matcher(result);
            matcher.matches();
            this.charging = !matcher.group(2).equals("NOT_CHARGING");
            System.out.println("charg: " + this.charging);

            // <input type="hidden" name="rngHvacOffNb" value="103600" id="rngHvacOffNb" />
            matcher = Pattern.compile("(.*) name=\"rngHvacOffNb\" value=\"(\\d+)\"(.*)").matcher(result);
            matcher.matches();
            int range_km = Integer.parseInt(matcher.group(2)) / 1000;
            System.out.println("rangekm: " + range_km);
            if (this.useMetric)
                this.range = Math.round(range_km) + " km";
            else
                this.range = Math.round(range_km * 0.621371) + " mi";

            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            this.lastUpdateTime = today.format("%Y-%m-%d %H:%M:%S");

            SharedPreferences.Editor editor = settings.edit();
            editor.putString("range", this.range);
            editor.putString("chargeTime", this.chargeTime);
            editor.putBoolean("charging", this.charging);
            editor.putString("chargerType", this.chargerType);
            editor.putString("lastupdate", this.lastUpdateTime);
            editor.putInt("currentBattery", this.currentBattery);
            editor.commit();

            return true;
        } catch (Exception e) {
            System.out.println("Failure!!!");
            System.out.println(e);
        }

        return false;
    }

    public boolean startAC(boolean desired) {
        CookieStore jar = this.login();

        if (jar == null) return false;

        String carid = this.getCarId(jar);

        String output = getHTTPString(url + "vehicles/setHvac?vin=" + carid + "&fan=" + (desired ? "on" : "off"), jar);

        if (!output.equals("true")) {
            Log.e(TAG, "Start AC failed. Output: " + output);
            return false;
        }

        return true;
    }

    private String getHTTPString(String url, CookieStore jar) {
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("User-Agent", UA);
            httpclient.setCookieStore(jar);

            HttpResponse response = httpclient.execute(httpget);

            InputStream inputStream = response.getEntity().getContent();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String result = "";

            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }

            inputStream.close();

            return result;
        } catch (Exception e) {
            System.out.println(e);
        }

        return "";
    }

}
