package me.jaxbot.wear.leafstatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MyActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        final Context context = this;

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("U", 0);
                SharedPreferences.Editor editor = settings.edit();

                String username = ((EditText) findViewById(R.id.txtUsername)).getText().toString();
                String password = ((EditText) findViewById(R.id.txtPassword)).getText().toString();
                int interval = ((SeekBar) findViewById(R.id.seekBar)).getProgress();

                editor.putString("username", username);
                editor.putString("password", password);
                editor.putInt("interval", interval);

                editor.commit();

                AlarmSetter.setAlarm(context);

                button.setEnabled(false);

                showToast("Saved, updating vehicle status...");
            }
        });

        SharedPreferences settings = getSharedPreferences("U", 0);

        int interval = settings.getInt("interval", 30);
        ((EditText) findViewById(R.id.txtUsername)).setText(settings.getString("username", ""));
        ((EditText) findViewById(R.id.txtPassword)).setText(settings.getString("password", ""));
        ((SeekBar) findViewById(R.id.seekBar)).setProgress(interval);
        setProgressText(interval);

        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setProgressText(i);
                ((Button) findViewById(R.id.button)).setEnabled(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setProgressText(int interval) {
        ((TextView) findViewById(R.id.txtMinutes)).setText("Update every " + (interval + 15) + " minutes");
    }

    void showToast(String text)
    {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }
}

