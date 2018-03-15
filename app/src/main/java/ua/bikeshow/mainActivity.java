package ua.bikeshow;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class mainActivity extends FragmentActivity implements IBaseGpsListener, OnMapReadyCallback {

    Button emergency, start_stop, btn_search;
    TextView speed, bpm;
    ImageButton settings;
    MapFragment mapFragment;

    LocationManager locationManager;
    GoogleMap googleMap = null;

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;

    final int TIME_SEG = 60;    // intervalo de tempo para leitura heart rate
    String speed_units = "km/h", bt_address;
    boolean ss = false, isListeningHeartRate = false, band_found = false, band_connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        initilaizeComponents();
        initializeEvents();

        getBoundedDevice();

        if (band_found){
            startConnecting();
            btn_search.setVisibility(View.GONE);
        }
        else
            bpm.setText("Not found");

        //writeDB();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.updateSpeed(null);

        mapFragment.getMapAsync(this);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);

        Timer myTimer = new Timer("MyTimer", true);
        myTimer.schedule(new MyTask(), calendar.getTime(), TIME_SEG * 1000);
    }

    void search_band(){
        getBoundedDevice();
        if (band_found){
            startConnecting();
            btn_search.setVisibility(View.GONE);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 10);
        }
        else
            bpm.setText("Not found");
    }

    private class MyTask extends TimerTask {

        public void run(){
            if(band_connected)
                startScanHeartRate();
        }

    }

    void getBoundedDevice() {
        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("MI Band 2")) {
                bt_address = bd.getAddress();
                band_found = true;
                bpm.setText("Found...");
            }
        }
        //band_found = false;
    }

    void startConnecting() {
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(bt_address);
        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);
    }

    void stateConnected() {
        bluetoothGatt.discoverServices();
        band_connected = true;
    }

    void stateDisconnected() {
        bluetoothGatt.disconnect();
        band_connected = false;
    }

    void startScanHeartRate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bpm.setText("...");
            }
        });

        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.controlCharacteristic);
        bchar.setValue(new byte[]{21, 2, 1});
        bluetoothGatt.writeCharacteristic(bchar);
    }

    void listenHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.measurementCharacteristic);
        bluetoothGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(CustomBluetoothProfile.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
        isListeningHeartRate = true;
    }

    void getBatteryStatus() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.Basic.service)
                .getCharacteristic(CustomBluetoothProfile.Basic.batteryCharacteristic);
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            Toast.makeText(this, "Failed get battery info", Toast.LENGTH_SHORT).show();
        }
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
        btn_search = (Button)findViewById(R.id.btn_search);
        settings = (ImageButton)findViewById(R.id.settings);
        speed = (TextView)findViewById(R.id.speed);
        bpm = (TextView)findViewById(R.id.bpm);
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

        btn_search.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    search_band();
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

    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.v("test", "onConnectionStateChange");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                stateConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stateDisconnected();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.v("test", "onServicesDiscovered");
            listenHeartRate();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.v("test", "onCharacteristicRead");
            byte[] data = characteristic.getValue();
            //bpm.setText(Arrays.toString(data));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v("test", "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.v("test", "onCharacteristicChanged");
            byte[] data = characteristic.getValue();
            String s = Arrays.toString(data);
            String[] parts = s.split(", ");
            final String[] parts2 = parts[1].split("]");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bpm.setText(parts2[0] + " BPM");
                }
            });
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.v("test", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.v("test", "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.v("test", "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.v("test", "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.v("test", "onMtuChanged");
        }

    };

}
