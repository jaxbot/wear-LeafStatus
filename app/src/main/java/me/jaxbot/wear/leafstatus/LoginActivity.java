package me.jaxbot.wear.leafstatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class LoginActivity extends Activity {
    final static String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.supported_countries, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        final Context context = this;
        final Activity activity = this;

        Configuration.init(context);

        final Button button = (Button) findViewById(R.id.button_signin);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                Configuration.username = ((EditText) findViewById(R.id.txtUsername)).getText().toString();
                Configuration.password = ((EditText) findViewById(R.id.txtPassword)).getText().toString();
                Configuration.carid = "";
                Configuration.portal = spinner.getSelectedItemPosition();
                Configuration.newOwnerVersion = true;
                Configuration.save(context);

                button.setEnabled(false);
                button.setText("Signing in...");

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        Log.d(TAG, "Calling carwings update...");

                        Carwings carwings = new Carwings(context);

                        if (carwings.trylogin()) {
                            Intent intent = new Intent(context, MyActivity.class);
                            startActivity(intent);
                            finish();
                        } else {

                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    button.setEnabled(true);
                                    button.setText("Login failed. Make sure you have migrated your account to the new backend.\n\nYou can do this by visiting http://owners.nissanusa.com/nowners/");
                                }
                            });

                            Log.d(TAG, "Update failed with an exception.");
                        }
                        return null;
                    }
                }.execute(null, null, null);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }
}

