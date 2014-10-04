package me.jaxbot.wear.leafstatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
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


        final SharedPreferences settings = getSharedPreferences("U", 0);

        final Button button = (Button) findViewById(R.id.button_signin);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                SharedPreferences.Editor editor = settings.edit();

                String username = ((EditText) findViewById(R.id.txtUsername)).getText().toString();
                String password = ((EditText) findViewById(R.id.txtPassword)).getText().toString();

                editor.putString("username", username);
                editor.putString("password", password);
                editor.putString("carid", "");
                editor.putInt("portal", spinner.getSelectedItemPosition());

                editor.commit();

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
                                    button.setText("Login failed, check username/password");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
