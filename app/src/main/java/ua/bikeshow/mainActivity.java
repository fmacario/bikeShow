package ua.bikeshow;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Formatter;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
//public class mainActivity extends AppCompatActivity implements IBaseGpsListener, OnMapReadyCallback {
public class mainActivity extends FragmentActivity implements IBaseGpsListener, OnMapReadyCallback {

    Button emergency, start_stop;
    TextView speed;
    ImageButton settings;
    MapFragment mapFragment;

    LocationManager locationManager;
    GoogleMap googleMap = null;

    String speed_units = "km/h";
    boolean ss = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initilaizeComponents();
        initializeEvents();

        //writeDB();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.updateSpeed(null);

        mapFragment.getMapAsync(this);

    }

    private void writeDB() {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference root = database.getReference(); //Getting root reference
        DatabaseReference session = root.child("session");

        DatabaseReference inicio = session.child("2").child("inicio");
        DatabaseReference fim = session.child("2").child("fim");
        inicio.setValue("Ramona");
        fim.setValue("UA");

        DatabaseReference vel = session.child("2").child("min").child("1").child("vel");
        DatabaseReference bat = session.child("2").child("min").child("1").child("bat");
        //DatabaseReference bat = session.child("2").child("min").child("1").child("bat");
        vel.setValue(40);
        bat.setValue(60);
    }

    private void initilaizeComponents() {
        emergency = (Button)findViewById(R.id.button_emergency);
        start_stop = (Button)findViewById(R.id.start_stop);
        settings = (ImageButton)findViewById(R.id.settings);
        speed = (TextView)findViewById(R.id.speed);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
    }


    private void initializeEvents() {
        settings.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    Intent i = new Intent(getApplicationContext(),Settings.class);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        emergency.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    Intent i = new Intent(getApplicationContext(), Emergency.class);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        start_stop.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    if ( ss == false ){
                        start_stop.setText("STOP");
                        ss = true;
                    }
                    else{
                        start_stop.setText("START");
                        ss = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateSpeed(CLocation location) {
        float nCurrentSpeed = 0;

        if(location != null)
        {
            location.setUnits( speed_units );
            nCurrentSpeed = location.getSpeed();
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        /*
        if(this.useMetricUnits())
        {
            strUnits = "meters/second";
        }
        */
        speed.setText(strCurrentSpeed + " " + speed_units);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

        googleMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null)
        {
            CLocation myLocation = new CLocation(location, speed_units);
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }
}
