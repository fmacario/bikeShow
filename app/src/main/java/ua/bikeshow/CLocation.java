package ua.bikeshow;

import android.location.Location;

/**
 * Created by filip on 05/03/2018.
 */

public class CLocation extends Location {
    public String units = "km/h";

    public CLocation(Location location)
    {
        this(location, "km/h");
    }

    public CLocation(Location location, String units) {
        // TODO Auto-generated constructor stub
        super(location);
        this.units = units;
    }


    public String getUnits()
    {
        return units;
    }

    public void setUnits(String units)
    {
        this.units = units;
    }

    @Override
    public float distanceTo(Location dest) {
        // TODO Auto-generated method stub
        float nDistance = super.distanceTo(dest);
        if(!this.units.equals("km/h"))
        {
            //Convert
            nDistance = nDistance / 1000f;
        }
        return nDistance;
    }

    @Override
    public float getAccuracy() {
        // TODO Auto-generated method stub
        float nAccuracy = super.getAccuracy();
        if(!this.units.equals("km/h"))
        {
            //Convert
            nAccuracy = nAccuracy / 1000;
        }
        return nAccuracy;
    }

    @Override
    public double getAltitude() {
        // TODO Auto-generated method stub
        double nAltitude = super.getAltitude();
        if(!this.units.equals("km/h"))
        {
            //Convert meters to feet
            nAltitude = nAltitude / 1000d;
        }
        return nAltitude;
    }

    @Override
    public float getSpeed() {
        // TODO Auto-generated method stub
        float nSpeed = super.getSpeed();
        if(!this.units.equals("km/h"))
        {
            //Convert meters/second to km/hour
            nSpeed = nSpeed * 3.6f;
        }
        return nSpeed;
    }

    @Override
    public double getLatitude() {
        return super.getLatitude();
    }

    @Override
    public double getLongitude() {
        return super.getLongitude();
    }
}