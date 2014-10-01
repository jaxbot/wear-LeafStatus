package me.jaxbot.wear.leafstatus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
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
        if (settings.getString("username", "").equals("") || true) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

            if (settings.getString("username", "").equals("") || true)
                finish();
        }

        setContentView(R.layout.activity_my);

        final Context context = this;

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                SharedPreferences.Editor editor = settings.edit();

                String username = ((EditText) findViewById(R.id.txtUsername)).getText().toString();
                String password = ((EditText) findViewById(R.id.txtPassword)).getText().toString();
                int interval = ((SeekBar) findViewById(R.id.seekBar)).getProgress();
                boolean checked = ((CheckBox) findViewById(R.id.checkBox)).isChecked();

                editor.putString("username", username);
                editor.putString("password", password);
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
        EditText txtUsername = (EditText)findViewById(R.id.txtUsername);
        EditText txtPassword = (EditText)findViewById(R.id.txtPassword);
        final SeekBar seekbar = (SeekBar)findViewById(R.id.seekBar);

        txtUsername.setText(settings.getString("username", ""));
        txtPassword.setText(settings.getString("password", ""));
        seekbar.setProgress(interval);
        setProgressText(interval);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                findViewById(R.id.button).setEnabled(true);
            }
        };

        txtUsername.addTextChangedListener(watcher);
        txtPassword.addTextChangedListener(watcher);

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
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
            INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }
}

