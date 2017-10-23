package com.developer.aashish.diwaliwork;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener, SeekBar.OnSeekBarChangeListener {


    private static final String TAG = "LocationAddress";
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 0;
    static String value = null;
    public TextView textView,valuelat,valuelang;
    LocationManager locationManager;
    Location loc;
    ProgressDialog dialog;
    ArrayList<String> permissions = new ArrayList<>();
    ArrayList<String> permissionsToRequest;
    boolean isGPS = false;
    boolean isNetwork = false;
    boolean isFromGps = true;
    boolean canGetLocation = true;



    private void showSettingsAlert() {
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=(TextView)findViewById(R.id.addresstext);
        valuelat=(TextView)findViewById(R.id.valeLat);
        valuelang=(TextView)findViewById(R.id.tvLang);
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
                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i)).append("\n");
                        }
//                        String add = address.getAddressLine(0);
//                        Log.d("Complete", add);
//                        sb.append(address.getAddressLine(0));
                        result = sb.toString();
                        valuelat.setText(latitude+"");
                        valuelang.setText(longitude+"");
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
                    getApplicationContext(), new GeocoderHandler());

            Toast.makeText(MainActivity.this, "Permission already granted.", Toast.LENGTH_LONG).show();

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
            int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (result == PackageManager.PERMISSION_GRANTED) {

                return true;

            } else {

                return false;

            }
        }

        private void requestPermission() {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                Toast.makeText(MainActivity.this, "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            switch (requestCode) {
                case PERMISSION_REQUEST_CODE:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    } else {

                        Toast.makeText(MainActivity.this, "Permission Denied, You cannot access location data.", Toast.LENGTH_LONG).show();

                    }
                    break;
            }
        }


//        public String[] getpath(JSONArray googleStepsJson)
//        {
//            int count=googleStepsJson.length();
//            String[] polyline=new String[count];
//            for(int i=0;i<count;i++)
//            {
//                try {
//                    polyline[i]= String.valueOf(getpath(googleStepsJson.getJSONArray(i)));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }return polyline;
//        }
//
//public String getpath(JSONObject googlePathJson)
//{String polyline=""
//    try {
//        String polyline=googlePathJson.getJSONObject("polyline").getString("points");
//    } catch (JSONException e) {
//        e.printStackTrace();
//    }
//return polyline;
//
//}


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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

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
}
