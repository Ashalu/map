package com.developer.aashish.diwaliwork;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,View.OnClickListener, LocationListener {
    private static final String TAG = "LocationAddress";
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 0;
    static String value = null;
   Button button;
    ArrayList<LatLng> latLngArrayList=new ArrayList<>();
    public TextView textView, valuelat, valuelang,tvdistance;
    LocationManager locationManager;
    Location loc;
    EditText etlat,etlang;
    ProgressDialog dialog;
    ArrayList<String> permissions = new ArrayList<>();
    ArrayList<String> permissionsToRequest;
    boolean isGPS = false;
    boolean isNetwork = false;
    boolean isFromGps = true;
    boolean canGetLocation = true;
double latitude,longitude;
    private void showSettingsAlert() {
    }

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        textView = (TextView) findViewById(R.id.addresstext);
        etlat = (EditText) findViewById(R.id.etlat);
        tvdistance=(TextView)findViewById(R.id.valuedistance);
        etlang = (EditText) findViewById(R.id.etlang);
        Button button=(Button)findViewById(R.id.cal);
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);
//        getLocation();
//        getAddressFromLocation(loc.getLatitude(), loc.getLongitude(),
//                getApplicationContext(), new GeocoderHandler());


        if (!isGPS && !isNetwork) {
            Log.d(TAG, "Connection off");
            showSettingsAlert();

        } else {
            Log.d(TAG, "Connection on");
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                    Log.d(TAG, "Permission requests");
                    canGetLocation = false;
                }
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                LatLng destination=new LatLng(Double.parseDouble(etlat.getText().toString()),Double.parseDouble(etlang.getText().toString()));
                LatLng sydney = new LatLng(loc.getLatitude(), loc.getLongitude());
                options.add(destination);
                options.add(sydney);
                mMap.addMarker(new MarkerOptions().position(destination)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Final Shop"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
                mMap.addPolyline(options);
             double totaal= CalculationByDistance(loc.getLatitude(),loc.getLongitude(),Double.parseDouble(etlat.getText().toString()),Double.parseDouble(etlang.getText().toString()));
                tvdistance.setText(totaal+"");
            }
        });
    }
    public void getAddressFromLocation(final double latitude, final double longitude,
                                       final Context context, final Handler handler) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i <= address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i)).append("\n");
                        }
//                        String add = address.getAddressLine(0);
//                        Log.d("Complete", add);
//                        sb.append(address.getAddressLine(0));
                        result = sb.toString();
                       // valuelat.setText(latitude + "");
                        //valuelang.setText(longitude + "");
                        Log.d("lat : ",latitude+"");
                        Log.d("lang : ",longitude+"");

                        textView.setText(result);
                        value = result;


                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable connect to Geocoder", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (result != null) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = "Latitude: " + latitude + " Longitude: " + longitude +
                                "\n\nAddress:\n" + result;
                        bundle.putString("address", result);
                        message.setData(bundle);
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = "Latitude: " + latitude + " Longitude: " + longitude +
                                "\n Unable to get address for this lat-long.";
                        bundle.putString("address", result);
                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        });

    }


    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (!checkPermission()) {

            requestPermission();

        } else {
            getLocation();

            getAddressFromLocation(loc.getLatitude(), loc.getLongitude(),
                    getApplicationContext(), new MapsActivity.GeocoderHandler());
 Toast.makeText(MapsActivity.this, "Permission already granted.", Toast.LENGTH_LONG).show();

        }

        return true;
    }

    public android.location.Location getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext()
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPS = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetwork = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPS && !isNetwork) {
                // location service disabled
            } else {
                this.canGetLocation = true;

                // if GPS Enabled get lat/long using GPS Services

                if (isGPS) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("GPS Enabled", "GPS Enabled");
                    }


                    if (locationManager != null) {
                        loc = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        //updateGPSCoordinates();
                    }
                }

                // First get location from Network Provider
                if (isNetwork) {
                    if (loc == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("Network", "Network");

                        if (locationManager != null) {
                            loc = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            //updateGPSCoordinates();
                        }
                    }

                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            Log.e("Error : Location",
                    "Impossible to connect to LocationManager", e);
        }

        return loc;
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;

        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            Toast.makeText(MapsActivity.this, "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {

                    Toast.makeText(MapsActivity.this, "Permission Denied, You cannot access location data.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }



    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View v) {

    }



    public class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
//            String[] value=locationAddress.split("@");
//            Intent intent=new Intent(UserLocation.this,ListOfCenters.class);
//            intent.putExtra("Latitude",Double.parseDouble(value[0]));
//            intent.putExtra("Longitude",Double.parseDouble(value[1]));
//            startActivity(intent);

        }
    }


//    You can use the Haversine algorithm. Another user has already coded a solution for this:

    public double CalculationByDistance(double initialLat, double initialLong,
                                        double finalLat, double finalLong){
        int R = 6371; // km
        double dLat = toRadians(finalLat-initialLat);
        double dLon = toRadians(finalLong-initialLong);
        latitude = toRadians(latitude);
        longitude = toRadians(longitude);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(latitude) * Math.cos(longitude);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    public double toRadians( double deg) {
        return deg * (Math.PI/180);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(loc.getLatitude(), loc.getLongitude());

        mMap.addMarker(new MarkerOptions().position(sydney).title(value));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


    }
}
