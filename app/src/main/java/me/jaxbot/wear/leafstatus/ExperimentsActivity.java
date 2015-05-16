package me.jaxbot.wear.leafstatus;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class ExperimentsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiments);

        final Context ctx = this;

        final Button campbtn = (Button)findViewById(R.id.campbtn);
        campbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Configuration.campModeOn) {
                    AlarmSetter.cancelAlarm(ctx);
                } else {
                    AlarmSetter.setCampAlarm(ctx);
                }
                Configuration.campModeOn = !Configuration.campModeOn;
                updateCampTitle(campbtn);
            }
        });
    }

    void updateCampTitle(Button campbtn) {
        if (Configuration.campModeOn)
            campbtn.setText(R.string.stop_camp_mode);
        else
            campbtn.setText(R.string.start_camp_mode);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_experiments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
