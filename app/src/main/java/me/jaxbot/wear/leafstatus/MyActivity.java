package me.jaxbot.wear.leafstatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

public class MyActivity extends ActionBarActivity {
    final Context that = this;
    private UpdatedReceiver receiver;

    private class UpdatedReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Carwings carwings = new Carwings(that);
            updateCarStatusUI(carwings);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        receiver = new UpdatedReceiver();

        Configuration.init(this);

        if (Configuration.username.equals("") || Configuration.vin.equals("")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_my);

        final Context context = this;

        final Spinner spinner = (Spinner) findViewById(R.id.spinner_chargelevel);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.charge_levels, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(Configuration.defaultChargeLevel);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                save();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                save();
                updateCarStatusAsync();

                button.setEnabled(false);

                showToast("Saved, updating vehicle status...");
            }
        });

        int interval = Configuration.interval;
        final SeekBar seekbar = (SeekBar)findViewById(R.id.seekBar);

        seekbar.setProgress(interval);
        setProgressText(interval);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setProgressText(i);
                findViewById(R.id.button).setEnabled(true);
                save();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        CheckBox permanent = (CheckBox)(findViewById(R.id.permanent));
        permanent.setChecked(Configuration.showPermanent);
        permanent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                save();
            }
        });

        CheckBox notifyonly = ((CheckBox)(findViewById(R.id.notifyonlycharging)));
        notifyonly.setChecked(Configuration.notifyOnlyWhenCharging);
        notifyonly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                save();
            }
        });

        CheckBox alwaysShowStartHVAC = ((CheckBox)(findViewById(R.id.alwaysshowstarthvac)));
        alwaysShowStartHVAC.setChecked(Configuration.alwaysShowStartHVAC);
        alwaysShowStartHVAC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                save();
            }
        });

        CheckBox checkbox = ((CheckBox)(findViewById(R.id.checkBox)));
        checkbox.setChecked(Configuration.autoUpdate);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                findViewById(R.id.button).setEnabled(true);
                seekbar.setEnabled(b);
                save();
            }
        });

        Carwings carwings = new Carwings(this);
        if (carwings.lastUpdateTime.equals("")) {
            updateCarStatusAsync();
        } else {
            updateCarStatusUI(carwings);
            LeafNotification.sendNotification(context, carwings);
        }
    }

    private void save() {
        int interval = ((SeekBar) findViewById(R.id.seekBar)).getProgress();
        boolean autoUpdate = ((CheckBox) findViewById(R.id.checkBox)).isChecked();
        boolean showPermanent = ((CheckBox) findViewById(R.id.permanent)).isChecked();
        boolean notifyOnlyWhenCharging = ((CheckBox) findViewById(R.id.notifyonlycharging)).isChecked();
        boolean alwaysShowStartHVAC = ((CheckBox) findViewById(R.id.alwaysshowstarthvac)).isChecked();
        final Spinner spinner = (Spinner) findViewById(R.id.spinner_chargelevel);

        if (showPermanent && !Configuration.showPermanent) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.wear_warning))
                    .setMessage(getString(R.string.undismissible))
                    .setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .show();
        }

        Configuration.interval = interval;
        Configuration.autoUpdate = autoUpdate;
        Configuration.showPermanent = showPermanent;
        Configuration.notifyOnlyWhenCharging = notifyOnlyWhenCharging;
        Configuration.alwaysShowStartHVAC = alwaysShowStartHVAC;
        Configuration.defaultChargeLevel = spinner.getSelectedItemPosition();

        Configuration.save(this);

        if (autoUpdate)
            AlarmSetter.setAlarm(this);
        else
            AlarmSetter.cancelAlarm(this);

        // Show the new notification, but don't talk to the server
        Carwings carwings = new Carwings(this);
        if (!carwings.lastUpdateTime.equals("")) {
            LeafNotification.sendNotification(this, carwings);
        }
    }

    private void updateCarStatusAsync()
    {
        (findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
        ShimmerFrameLayout container = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container);
        container.startShimmerAnimation();

        final Context context = this;
        final Activity activity = this;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                final Carwings carwings = new Carwings(context);

                carwings.update();
                return null;
            }
        }.execute(null, null, null);
    }

    private void updateCarStatusUI(Carwings carwings)
    {
        Resources res = getResources();

        (findViewById(R.id.surfaceView2)).setBackgroundColor(res.getColor(
                carwings.currentBattery == 12 ? R.color.green :
                        carwings.charging ? R.color.orange :
                                carwings.currentBattery > 2 ? R.color.blue : R.color.red));

        (findViewById(R.id.progressBar)).setVisibility(View.GONE);
        ShimmerFrameLayout container = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container);
        container.stopShimmerAnimation();

        ((TextView) findViewById(R.id.battery_bars)).setText(
                String.format(res.getString(R.string.charge_status_battery_bars), carwings.currentBattery));
        ((TextView) findViewById(R.id.chargetime)).setText(
                String.format(res.getString(carwings.charging ? R.string.charge_status_charging
                : R.string.charge_status_not_charging), carwings.chargeTime, carwings.chargerType));

        ((TextView) findViewById(R.id.range)).setText(carwings.range);
        ((TextView) findViewById(R.id.lastupdated)).setText(carwings.lastUpdateTime);
        findViewById(R.id.button).setEnabled(true);
    }

    private void setProgressText(int interval) {
        ((TextView) findViewById(R.id.txtMinutes)).setText("Update every " + (interval + 15) +
                " minutes");
    }

    private void showToast(String text)
    {
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
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
        if (id == R.id.action_signoff) {
            Configuration.signOff(this);

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        if (id == R.id.action_experiments) {
            Intent intent = new Intent(this, ExperimentsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentfilter = new IntentFilter("leafstatus.update");
        this.registerReceiver(receiver, intentfilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }
}

