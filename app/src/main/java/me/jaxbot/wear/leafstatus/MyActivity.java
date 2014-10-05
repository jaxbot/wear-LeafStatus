package me.jaxbot.wear.leafstatus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MyActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences settings = getSharedPreferences("U", 0);
        if (settings.getString("username", "").equals("")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_my);

        final Context context = this;

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = settings.edit();

                int interval = ((SeekBar) findViewById(R.id.seekBar)).getProgress();
                boolean checked = ((CheckBox) findViewById(R.id.checkBox)).isChecked();

                editor.putInt("interval", interval);
                editor.putBoolean("autoupdate", checked);

                editor.commit();

                if (checked)
                    AlarmSetter.setAlarm(context);
                else
                {
                    AlarmSetter.cancelAlarm(context);
                    Intent service = new Intent(context, UpdateCarwingsService.class);
                    context.startService(service);
                }

                button.setEnabled(false);

                showToast("Saved, updating vehicle status...");
            }
        });

        int interval = settings.getInt("interval", 30);
        final SeekBar seekbar = (SeekBar)findViewById(R.id.seekBar);

        seekbar.setProgress(interval);
        setProgressText(interval);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setProgressText(i);
                findViewById(R.id.button).setEnabled(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        CheckBox checkbox = ((CheckBox)(findViewById(R.id.checkBox)));
        checkbox.setChecked(settings.getBoolean("autoupdate", true));
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                findViewById(R.id.button).setEnabled(true);
                seekbar.setEnabled(b);
            }
        });

        Carwings carwings = new Carwings(this);
        ((TextView) findViewById(R.id.battery_bars)).setText(carwings.currentBattery);
        ((TextView) findViewById(R.id.chargetime)).setText(carwings.chargeTime);
        ((TextView) findViewById(R.id.range)).setText(carwings.currentBattery);
    }

    private void setProgressText(int interval) {
        ((TextView) findViewById(R.id.txtMinutes)).setText("Update every " + (interval + 15) + " minutes");
    }

    void showToast(String text)
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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

