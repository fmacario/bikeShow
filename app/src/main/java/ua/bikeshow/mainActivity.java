package ua.bikeshow;

import android.Manifest;
import android.Manifest.permission;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class mainActivity extends FragmentActivity implements IBaseGpsListener, OnMapReadyCallback {

    Button emergency, start_stop, btn_search;
    TextView speed, bpm;
    ImageButton settings;
    MapFragment mapFragment;
    ProgressBar bateria;

    LocationManager locationManager;
    GoogleMap googleMap = null;

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;

    final int TIME_SEG = 60;    // intervalo de tempo para leitura heart rate
    String speed_units = "km/h", bt_address;
    boolean ss = false, isListeningHeartRate = false, band_found = false, band_connected = false;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String user;
    private String currentSpeed="0.0";
    private String currentBpm="0.0";
    private String time;
    private int sessionMin;
    private boolean onSession = false;
    static boolean flag = false;
    private ArrayList<String> speeds =new ArrayList<String>();
    private ArrayList<String> bpms = new ArrayList<String>();
    public static LatLng latlng;

    final Context context = this;
    private static final int MY_PERMISSIONS_REQUEST = 100;

    Timer myTimer1 = new Timer("MyTimer1", true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences("settings", 0);
        String settings_speed_units = settings.getString("speed", " ");
        int emergency_number = settings.getInt("emergency_number", 0);

        if ( settings_speed_units.equalsIgnoreCase(" " ) ){
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("speed", "km/h");
            editor.apply();
            speed_units = "km/h";
        }else
            speed_units = settings_speed_units;

        if(!flag){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            flag = true;
        }
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if(mFirebaseUser == null){
           startActivity(new Intent(mainActivity.this, Login.class));
            finish();
            return;
        }

        user = FirebaseAuth.getInstance().getCurrentUser().getUid();


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        initializeComponents();
        initializeEvents();

        getBoundedDevice();

        if (band_found){
            startConnecting();
            btn_search.setVisibility(View.GONE);
        }
        else
            bpm.setText("Not found");



        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.updateSpeed(null);

        mapFragment.getMapAsync(this);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 1);

        Timer myTimer = new Timer("MyTimer", true);
        Timer myTimer2 = new Timer("MyTimer2", true);
        myTimer.schedule(new MyTask(), calendar.getTime(), TIME_SEG * 1000);
        calendar.add(Calendar.SECOND, 1);
        myTimer2.schedule(new MyTask2(), calendar.getTime(), TIME_SEG * 1000);

    }

    private void askForPermission() {
        String[] permissions = new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE};
        ActivityCompat.requestPermissions(mainActivity.this, permissions, MY_PERMISSIONS_REQUEST);
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
            if(band_connected){
                //startScanHeartRate();
                getBatteryStatus();

            }

        }

    }

    private class MyTask2 extends TimerTask {

        public void run(){
            if(band_connected){
                startScanHeartRate();
                //getBatteryStatus();
            }

        }

    }

    private class MyTask1 extends TimerTask {

        public void run(){
            if(onSession==true) {
                writeDB();
                speeds.add(currentSpeed);
                bpms.add(currentBpm);
                sessionMin++;
            }


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
        //Log.v("BATERIA", "onConnectionStateChange");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.Basic.service)
                .getCharacteristic(CustomBluetoothProfile.Basic.batteryCharacteristic);
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            //Toast.makeText(this, "Failed get battery info", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeDB() {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference root = database.getReference(); //Getting root reference
        DatabaseReference userDB = root.child("values").child(user);

        DatabaseReference session = userDB.child(time);
        String aux = Integer.toString(sessionMin);
        DatabaseReference sessionMinutes = session.child(aux);

       // int aux1 = Integer.parseInt(currentSpeed);
        DatabaseReference vel = sessionMinutes.child("vel");
        //int aux2 = Integer.parseInt(currentBpm);
        DatabaseReference bat = sessionMinutes.child("bat");

        vel.setValue(currentSpeed);
        bat.setValue(currentBpm);
    }

    private void writeSessionDB(Session aux){
        FirebaseDatabase dataBase = FirebaseDatabase.getInstance();
        DatabaseReference sess = dataBase.getReference().child("infos").child(user).child(time);
        sess.setValue(aux);


    }

    private void initializeComponents() {
        emergency = (Button)findViewById(R.id.button_emergency);
        start_stop = (Button)findViewById(R.id.start_stop);
        btn_search = (Button)findViewById(R.id.btn_search);
        settings = (ImageButton)findViewById(R.id.settings);
        speed = (TextView)findViewById(R.id.speed);
        bpm = (TextView)findViewById(R.id.bpm);
        bateria = (ProgressBar)findViewById(R.id.bateria);
        bateria.setScaleY(6f);
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

                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(mainActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST);
                    }

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
                        Date date = Calendar.getInstance().getTime();
                        String year = (String) android.text.format.DateFormat.format("yyyy", date);
                        String  month = (String) android.text.format.DateFormat.format("MM", date);
                        String day = (String) android.text.format.DateFormat.format("dd", date);
                        String  hour = (String) android.text.format.DateFormat.format("HH", date);
                        String min = (String) android.text.format.DateFormat.format("mm", date);
                        String sec = (String) android.text.format.DateFormat.format("ss", date);
                        time = year + month + day + hour + min + sec;
                        onSession=true;
                        sessionMin=0;
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.SECOND, 10);
                        myTimer1.schedule(new MyTask1(), calendar.getTime(), TIME_SEG * 1000);
                        ss = true;
                    }
                    else{
                        start_stop.setText("START");
                        onSession=false;
                        Double[] i= calculateResults();
                        Session aux = new Session(i[0],i[1],sessionMin-1);
                        writeSessionDB(aux);
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



    private Double[] calculateResults(){
        Double[] medias =new Double[2];
        Double aux=0.0;
        Double aux1=0.0;

        for(int i=0; i<speeds.size();i++) {
            aux+=Double.parseDouble(speeds.get(i));
        }
        medias[0]=aux/speeds.size();
        for(int j=0; j<bpms.size();j++) {
            aux1+=Double.parseDouble(bpms.get(j));
        }
        medias[1]=aux1/bpms.size();
        return medias;
    }

    private void updateSpeed(CLocation location) {
        float nCurrentSpeed = 0;

        if(location != null)
        {
            location.setUnits( speed_units );
            nCurrentSpeed = location.getSpeed();
            latlng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera( CameraUpdateFactory.newLatLng( latlng ) );
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
        currentSpeed = strCurrentSpeed;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setMapToolbarEnabled(true);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                googleMap.clear();

                //Log.d("DEBUG","Map clicked [" + point.latitude + " / " + point.longitude + "]");
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(point.latitude, point.longitude))
                        .title("Destino"));

                //Do your stuff with LatLng here
                //Then pass LatLng to other activity
            }
        });

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
            String s = Arrays.toString(data);
            String[] parts = s.split(", ");
            final String[] parts2 = parts[1].split("]");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bateria.setProgress(Integer.parseInt( parts2[0] ));
                }
            });

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
                    currentBpm=parts2[0];
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
