package ua.bikeshow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Settings extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    ImageButton backToMain;
    Button save;
    Button signout;
    Button sessions;
    Switch switch_speed;
    EditText number;
    SharedPreferences settings;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = getSharedPreferences("settings", 0);
        String speed_units = settings.getString("speed", " ");
        int settings_number = settings.getInt("emergency_number", 0);

        initilizeComponents();
        initializeEvents();

        if ( speed_units.equalsIgnoreCase("km/h") )
            switch_speed.setChecked(false);
        else
            switch_speed.setChecked(true);

        if ( settings_number != 0 )
            number.setText( String.valueOf(settings_number));
    }

    private void initilizeComponents() {
        backToMain = (ImageButton)findViewById(R.id.backToMain);
        save = (Button)findViewById(R.id.save);
        signout = (Button) findViewById(R.id.btn_sign);
        sessions = (Button) findViewById(R.id.btn_sess);
        switch_speed = (Switch) findViewById(R.id.units_speed);
        number = (EditText) findViewById(R.id.text_number);
    }

    private void initializeEvents() {
        backToMain.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    SharedPreferences.Editor editor = settings.edit();

                    if ( !switch_speed.isChecked() )
                        editor.putString("speed", "km/h");
                    else
                        editor.putString("speed", "m/h");

                    if ( number.getText().toString().length() != 0 )
                        editor.putInt("emergency_number", Integer.parseInt( number.getText().toString() ) );
                    else
                        editor.putInt("emergency_number", 0);

                    editor.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        signout.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    mAuth.signOut();
                    Intent i = new Intent(getApplicationContext(), mainActivity.class);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        sessions.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    Intent i = new Intent(getApplicationContext(), SessionsActivity.class);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
