package ua.bikeshow;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Emergency extends Activity {


    final Context context = this;
    private Button button;
    Button backToMain;
    private String PhoneNumber = "112";
    private static final int MY_PERMISSIONS_REQUEST = 100;
    SharedPreferences settings;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        askForPermission();

        // add PhoneStateListener
        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);


        initializeComponents();
        initializeEvents();
    }

    private void askForPermission() {
        String[] permissions = new String[]{Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE};
        ActivityCompat.requestPermissions(Emergency.this, permissions, MY_PERMISSIONS_REQUEST);
    }

    private void initializeComponents() {

        backToMain = (Button) findViewById(R.id.backToMain);
        button = (Button) findViewById(R.id.buttonCall);

    }

    private void initializeEvents() {
        backToMain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LatLng latlng = mainActivity.latlng;
                try {

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Emergency.this, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST);
                    }
                    else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Emergency.this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST);
                    }
                    else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Emergency.this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST);
                    }

                    SmsManager sms = SmsManager.getDefault();

                    settings = getSharedPreferences("settings", 0);
                    int settings_number = settings.getInt("emergency_number", 0);

                    String msg;

                    try{
                        msg = "Tive um acidente! Coordenadas: \n" + latlng.toString();
                    }catch ( Exception e){
                        msg = "Tive um acidente! Coordenadas: \n" ;
                    }

                    if ( settings_number != 0 && settings_number+"".length() != 0 ) {
                        try {
                            sms.sendTextMessage(settings_number + "", null, msg, null, null);
                            Toast toast = Toast.makeText(context, "Message sent.", Toast.LENGTH_SHORT);
                            toast.show();
                        } catch (Exception e) {

                        }
                    }
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + PhoneNumber)));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


        //monitor phone call activities
    private class PhoneCallListener extends PhoneStateListener {

        private boolean isPhoneCalling = false;

        String LOG_TAG = "LOGGING 123";

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
                Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                Log.i(LOG_TAG, "OFFHOOK");

                isPhoneCalling = true;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {
                // run when class initial and phone call ended,
                // need detect flag from CALL_STATE_OFFHOOK
                Log.i(LOG_TAG, "IDLE");

                if (isPhoneCalling) {

                    Log.i(LOG_TAG, "restart app");

                    // restart app
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(
                                    getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                    isPhoneCalling = false;
                }

            }
        }
    }

    }

