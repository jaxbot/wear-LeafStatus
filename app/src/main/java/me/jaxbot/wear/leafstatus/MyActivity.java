package me.jaxbot.wear.leafstatus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MyActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 2, intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Start after 60 seconds, run every hour
        am.setRepeating(AlarmManager.RTC_WAKEUP, 60000, 3600000, sender);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("U", 0);
                SharedPreferences.Editor editor = settings.edit();

                String username = ((EditText) findViewById(R.id.txtUsername)).getText().toString();
                String password = ((EditText) findViewById(R.id.txtPassword)).getText().toString();

                editor.putString("username", username);
                editor.putString("password", password);

                editor.commit();
            }
        });

        SharedPreferences settings = getSharedPreferences("U", 0);

        ((EditText) findViewById(R.id.txtUsername)).setText(settings.getString("username", ""));
        ((EditText) findViewById(R.id.txtPassword)).setText(settings.getString("password", ""));
    }
}

