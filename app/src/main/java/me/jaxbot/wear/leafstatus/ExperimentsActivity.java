package me.jaxbot.wear.leafstatus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class ExperimentsActivity extends ActionBarActivity {
    String TAG = "ExperimentsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiments);

        final Context ctx = this;
        final Button campBtn = (Button)findViewById(R.id.campbtn);
        campBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Configuration.campModeOn) {
                    Configuration.campModeOn = false;
                    AlarmSetter.cancelCampAlarm(ctx);
                    CampModeNotification.hideNotification(ctx);
                } else {
                    boolean success = AlarmSetter.setCampAlarm(ctx);
                    Log.i(TAG, success ? "Success!" : "Not success");
                    if (!success) {
                        CampModeNotification.hideNotification(ctx);
                        new AlertDialog.Builder(ctx)
                                .setTitle(getString(R.string.battery_too_low))
                                .setMessage(getString(R.string.battery_too_low_desc))
                                .setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();
                    } else {
                        CampModeNotification.showNotification(ctx);
                        Configuration.campModeOn = true;
                        Toast toast = Toast.makeText(ctx, "Starting A/C camp mode...", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                Configuration.save(ctx);
                updateCampTitle(campBtn);
            }
        });

        // Audit the camp mode state
        if (System.currentTimeMillis() - Configuration.campModeLastRun > 1000 * 60 * 15) {
            Log.i(TAG, "Cowardly turning off camp mode, since it hasn't been running.");
            Configuration.campModeOn = false;
            Configuration.save(this);
            CampModeNotification.hideNotification(this);
        }

        updateCampTitle(campBtn);
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
