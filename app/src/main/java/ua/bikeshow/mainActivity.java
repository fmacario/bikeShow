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

import java.util.Formatter;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
//public class mainActivity extends AppCompatActivity implements IBaseGpsListener, OnMapReadyCallback {
public class mainActivity extends FragmentActivity implements IBaseGpsListener, OnMapReadyCallback {
    private GoogleMap googleMap = null;
    TextView speed;
    String speed_units = "km/h";
    boolean ss = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton settings = (ImageButton)findViewById(R.id.settings);
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

        Button emergency = (Button)findViewById(R.id.button_emergency);
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

        final Button start_stop = (Button)findViewById(R.id.start_stop);
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

        speed = (TextView)findViewById(R.id.speed);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.updateSpeed(null);

        speed.setText("0 " + speed_units);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void updateSpeed(CLocation location) {
        // TODO Auto-generated method stub
        float nCurrentSpeed = 0;

        if(location != null)
        {
            location.setUnits( speed_units );
            nCurrentSpeed = location.getSpeed();
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        //strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        /*
        if(this.useMetricUnits())
        {
            strUnits = "meters/second";
        }
*/
        speed.setText(strCurrentSpeed + " " + speed_units);
/*
        if ( googleMap != null ){
         googleMap.addMarker(new MarkerOptions()
                .position(new LatLng( location.getLatitude(), location.getLongitude()))
                .title("Marker"));

        }
*/
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
