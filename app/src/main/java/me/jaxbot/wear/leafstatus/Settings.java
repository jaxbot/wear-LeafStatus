package me.jaxbot.wear.leafstatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;


public class Settings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Context context = this;

        Button signoff = (Button)findViewById(R.id.button_signoff);
        signoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences settings = getSharedPreferences("U", 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("username", "");
                editor.putString("password", "");

                editor.commit();

                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        CheckBox metric = (CheckBox)(findViewById(R.id.metric));
        metric.setChecked(Configuration.useMetric);
        metric.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                save();
            }
        });

        CheckBox nightupdates = ((CheckBox)(findViewById(R.id.nightupdates)));
        nightupdates.setChecked(Configuration.noNightUpdates);
        nightupdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                save();
            }
        });

    }

    void save() {
        boolean useMetric = ((CheckBox) findViewById(R.id.metric)).isChecked();
        boolean noNightUpdates = ((CheckBox) findViewById(R.id.nightupdates)).isChecked();
        Configuration.useMetric = useMetric;
        Configuration.noNightUpdates = noNightUpdates;
        Configuration.save(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
