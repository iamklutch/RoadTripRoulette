package com.yukidev.roadtriproulette;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.os.Process;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;

import java.io.IOException;
import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private double mLongitude;
    private double mLatitude;
    private double mDesiredDistance;
    private String mDesiredDirection;
    private String cityName;
    private Location lastKnownLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    @Bind (R.id.cityEditText) EditText mLocationEditText;
    @Bind (R.id.letsGoImageButton)ImageButton mLetsGoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mLocationEditText = (EditText)findViewById(R.id.cityEditText);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(5 * 1000)
                .setFastestInterval(1 * 1000);


        mLetsGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDesiredDistance = milesToLatLng(25);
                mDesiredDirection = "N";

                if (isNetworkAvailable()) {

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(getDirectionUrl(mLatitude,mLongitude,mDesiredDirection))
                            .build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });
                            //  Alert user about error
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });

                            try {
                                String jsonData = response.body().string();
                                if (response.isSuccessful()) {
//                                    mForecast = parseForecastDetails(jsonData);
                                    // runOnUiThread allows the data on the background thread go to the Main UI thread.
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // if it worked, this is were you do your shit
                                        }
                                    });

                                } else {
                                    //   alert user about error
                                    Log.v(TAG, response.body().string());
                                }
                            }
                            catch (IOException e) {
                                Log.e(TAG, "Exception caught: ", e);
                            }
//                            catch (JSONException e){
//                                Log.e(TAG, "Exception caught: ", e);
//                            }
                        }
                    });


                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.network_unavailable),
                            Toast.LENGTH_LONG).show();
                }


//                Thread yelp = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String[] args = new String[4];
//                        args[0] = "--term";
//                        args[1] = "tacos";
//                        args[2] = "--ll";
//                        args[3] = mLatitude + " , " + mLongitude;
//
//                        YelpAPI.main(args);
//                    }
//                });
//
//                yelp.start();
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        } else {
            handleNewLocation(location);
        }
    }

    private void handleNewLocation (Location location) {

        mLongitude = location.getLongitude();
        mLatitude = location.getLatitude();

        mLocationEditText.setHint(mLongitude + " , " + mLatitude);
        Log.d(TAG, location.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services disconnected, please reconnect");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public String getDirectionUrl (double latitude, double longitude, String direction) {
        double newLat = latitude;
        double newLng = longitude;
        String directionURL;
        switch (direction) {
            case "N":
                newLat = latitude + mDesiredDistance;
                newLng = longitude;
                break;
            case "S":
                newLat = latitude - mDesiredDistance;
                newLng = longitude;
                break;
            case "E":
                newLat = latitude;
                newLng = longitude + mDesiredDistance;
                break;
            case "W":
                newLat = latitude;
                newLng = longitude - mDesiredDistance;
                break;
        }

        directionURL = "http://api.geonames.org/findNearbyPlaceNameJSON?lat=" + newLat +
                "&lng=" + newLng + "&username=yukidev&style=MEDIUM&cities=cities15000&radius=300";

        return directionURL;
    }

    public double milesToLatLng (int miles) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0000");
        double latLng;
        double km;
        km = Math.round(miles / 0.62137);
        latLng = km * .00904977;
        decimalFormat.format(latLng);
        return latLng;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }
}
