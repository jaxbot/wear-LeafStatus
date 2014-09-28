package me.jaxbot.wear.leafstatus;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jonathan on 9/21/14.
 */
public class Carwings {
    private String username;
    private String password;

    public int currentBattery;
    public String chargeTime;
    public boolean currentHvac;

    SharedPreferences settings;

    // Disgusting, but we're using the web frontend, and thus will pretend
    String UA = "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";

    public Carwings(Context context)
    {
        settings = context.getSharedPreferences("U", 0);
        this.username = settings.getString("username", "");
        this.password = settings.getString("password", "");
        this.currentBattery = settings.getInt("currentBattery", 0);
        this.chargeTime = settings.getString("chargeTime", "");
    }
    private CookieStore login() {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost("https://www.nissanusa.com/owners/j_spring_security_check");
        httppost.setHeader("User-Agent", UA);

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("j_username", username));
            nameValuePairs.add(new BasicNameValuePair("j_passwordHolder", "Password"));
            nameValuePairs.add(new BasicNameValuePair("j_password", password));
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

    private String getCarId(CookieStore jar) {
        // Check if we have already grabbed this
        String cachedCarID = settings.getString("carid", "");
        if (!cachedCarID.equals(""))
            return cachedCarID;

        // This is a particularly bad and non-future-safe operation,
        // but so is the entire application, since Nissan's API is internal
        String vehicleHTML = getHTTPString("https://www.nissanusa.com/owners/vehicles", jar);
        Pattern pattern = Pattern.compile("(.*)div class=\"vehicleHeader\" id=\"(\\d+)\"(.*)");
        Matcher m = pattern.matcher(vehicleHTML);
        if (m.matches()) {
            cachedCarID = m.group(2);

            // Save it, since it is unlikely to change
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("carid", cachedCarID);
            editor.commit();

            return cachedCarID;
        } else {
            Log.e("Leaf", "Failed to find vehicle id");
            return "";
        }
    }

    public boolean update() {
        try {
            CookieStore jar = this.login();
            String carid = this.getCarId(jar);

            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("https://www.nissanusa.com/owners/vehicles/statusRefresh?id=" + carid);
            httpget.setHeader("User-Agent", UA);
            httpclient.setCookieStore(jar);
            httpclient.execute(httpget);

            String result = getHTTPString("https://www.nissanusa.com/owners/vehicles/pollStatusRefresh?id=" + carid, jar);

            JSONObject jObject = new JSONObject(result);
            this.currentBattery = jObject.getInt("currentBattery");
            this.chargeTime = jObject.getString("chargeTime");

            if (chargeTime.equals("null"))
                this.chargeTime = jObject.getString("chargeTime220");

            if (chargeTime.equals("null"))
                this.chargeTime = jObject.getString("chrgDrtn22066Tx");

            if (chargeTime.equals("null"))
                this.chargeTime = "Unknown";

            this.currentHvac = jObject.getBoolean("currentHvac");

            SharedPreferences.Editor editor = settings.edit();
            editor.putString("chargeTime", this.chargeTime);
            editor.putInt("currentBattery", this.currentBattery);
            editor.commit();

            return true;
        } catch (Exception e) {
            System.out.println(e);
        }

        return false;
    }

    public boolean startAC(boolean desired) {
        CookieStore jar = this.login();

        if (jar == null) return false;

        String carid = this.getCarId(jar);

        getHTTPString("https://www.nissanusa.com/owners/vehicles/setHvac?id=" + carid + "&fan=" + (desired ? "on" : "off"), jar);

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
            while ((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();

            return result;
        } catch (Exception e) {
            System.out.println(e);
        }

        return "";
    }

}
