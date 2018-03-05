package ua.bikeshow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.Console;
import java.util.Formatter;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class mainActivity extends AppCompatActivity implements IBaseGpsListener {
    TextView speed;
    String speed_units = "km/h";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        speed = (TextView)findViewById(R.id.speed);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.updateSpeed(null);

        speed.setText("0 " + speed_units);
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
}
