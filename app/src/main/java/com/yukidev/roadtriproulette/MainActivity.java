package com.yukidev.roadtriproulette;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private double mCurrentLongitude;
    private double mCurrentLatitude;
    private double mYelpLat;
    private double mYelpLng;
    private double mDesiredDistance;
    private String mDesiredDirection;
    Context mContext;

    private GeoNameData mGeoNameData;

    private Location lastKnownLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    @Bind (R.id.searchTermEditText)EditText mSearchTermEditText;
    @Bind (R.id.letsGoImageButton)ImageButton mLetsGoButton;
    @Bind (R.id.directionSpinner)Spinner mDirectionSpinner;
    @Bind (R.id.distanceEditText)EditText mDistanceEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mContext = this;

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

                if (Integer.parseInt(mDistanceEditText.getText().toString()) == 0){
                    Random distRandom = new Random();
                    int dist = (distRandom.nextInt(90) + 10);
                    mDesiredDistance = milesToLatLng(dist);
                } else {
                    int distance = Integer.parseInt(mDistanceEditText.getText().toString());
                    mDesiredDistance = milesToLatLng(distance);
                }

                if (mDirectionSpinner.getSelectedItemPosition() == 0){
                    Random dirRandom = new Random();

                    mDesiredDirection = mDirectionSpinner
                            .getItemAtPosition(dirRandom.nextInt(8)+1).toString();
                } else {
                    mDesiredDirection = mDirectionSpinner.getSelectedItem().toString();
                }

                if (isNetworkAvailable()) {

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(getDirectionUrl(mCurrentLatitude, mCurrentLongitude,mDesiredDirection))
//                            .url(getDirectionUrl(49.5197,7.6808 , mDesiredDirection))
                            .build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    double[] yelpLatLng = yelpOnlyPoints
                                            (mCurrentLatitude, mCurrentLongitude, mDesiredDirection);
                                    yelpIt(yelpLatLng[0], yelpLatLng[1]);

                                }
                            });
                            //  Alert user about error
                            Log.d(TAG, "okHttp geoNames Failed");
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
                                    mGeoNameData = parsePlaceDetails(jsonData);
                                    // runOnUiThread allows the data on the background thread go to the Main UI thread.
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Cities[] cities = mGeoNameData.getCities();

                                            Random random = new Random();
                                            if (cities.length <= 0){
                                                yelpOnlyPoints(mCurrentLatitude, mCurrentLongitude
                                                        ,mDesiredDirection);
                                            } else {
                                                int i = random.nextInt(cities.length);
                                                mYelpLat = cities[i].getLat();
                                                mYelpLng = cities[i].getLng();
                                                if (mYelpLat <= 0 || mYelpLng <= 0) {
                                                    yelpOnlyPoints(mCurrentLatitude, mCurrentLongitude
                                                            ,mDesiredDirection);
                                                }
                                            }

                                            yelpIt(mYelpLat, mYelpLng);

//                                            Thread yelp = new Thread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                String[] args = new String[4];
//                                                args[0] = "--term";
//                                                args[1] = "dinner";
//                                                args[2] = "--ll";
//                                                args[3] = mYelpLat + " , " + mYelpLng;
//
//                                                YelpAPI.main(args, mContext);
//                                            }
//                                        });
//
//                                        yelp.start();
                                        }
                                    });

                                } else {
                                    //   alert user about error
                                    Log.v(TAG, jsonData.toString());
                                }
                            }
                            catch (IOException e) {
                                Log.e(TAG, "Exception caught: ", e);
                            }
                            catch (JSONException e){
                                Log.e(TAG, "Exception caught: ", e);
                            }
                        }
                    });


                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.network_unavailable),
                            Toast.LENGTH_LONG).show();
                }
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

        mCurrentLongitude = location.getLongitude();
        mCurrentLatitude = location.getLatitude();

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
            case "NE":
                newLat = latitude + (mDesiredDistance * .8);
                newLng = longitude + (mDesiredDistance * .8);
                break;
            case "NW":
                newLat = latitude + (mDesiredDistance * .8);
                newLng = longitude - (mDesiredDistance * .8);
                break;
            case "S":
                newLat = latitude - mDesiredDistance;
                newLng = longitude;
                break;
            case "SE":
                newLat = latitude - (mDesiredDistance * .8);
                newLng = longitude + (mDesiredDistance * .8);
                break;
            case "SW":
                newLat = latitude - (mDesiredDistance * .8);
                newLng = longitude - (mDesiredDistance * .8);
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
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(8);
        decimalFormat.format(newLat);
        decimalFormat.format(newLng);

        directionURL = "http://api.geonames.org/findNearbyPlaceNameJSON?lat=" + newLat +
                "&lng=" + newLng + "&username=yukidev&style=MEDIUM&cities=cities1000&radius=100";

        return directionURL;
    }

    public double milesToLatLng (int miles) {
        DecimalFormat decimalFormat = new DecimalFormat();
        double latLng;
        double km;
        km = Math.round(miles / 0.62137);
        latLng = km * .00904977;
        decimalFormat.setMaximumFractionDigits(8);
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

    private GeoNameData parsePlaceDetails (String jsonData) throws JSONException{
        GeoNameData geoNameData = new GeoNameData();

        geoNameData.setCities(getCities(jsonData));

        return geoNameData;
    }

    private Cities[] getCities(String jsonData) throws JSONException {
        JSONObject cities = new JSONObject(jsonData);
        JSONArray data = cities.getJSONArray("geonames");

        Cities[] city = new Cities[data.length()];
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonCity = data.getJSONObject(i);
            Cities cityInfo = new Cities();

            cityInfo.setName(jsonCity.getString("name"));
            cityInfo.setLat(jsonCity.getDouble("lat"));
            cityInfo.setLng(jsonCity.getDouble("lng"));
            cityInfo.setDistance(jsonCity.getDouble("distance"));

            city[i] = cityInfo;
        }

        return city;
    }

    private double[] yelpOnlyPoints (double latitude, double longitude, String direction) {
        DecimalFormat decimalFormat = new DecimalFormat();
        double[] yelpLatLng = new double[2];
        double newLat = latitude;
        double newLng = longitude;

        switch (direction) {
            case "N":
                newLat = latitude + mDesiredDistance;
                newLng = longitude;
                break;
            case "NE":
                newLat = latitude + (mDesiredDistance * .8);
                newLng = longitude + (mDesiredDistance * .8);
                break;
            case "NW":
                newLat = latitude + (mDesiredDistance * .8);
                newLng = longitude - (mDesiredDistance * .8);
                break;
            case "S":
                newLat = latitude - mDesiredDistance;
                newLng = longitude;
                break;
            case "SE":
                newLat = latitude - (mDesiredDistance * .8);
                newLng = longitude + (mDesiredDistance * .8);
                break;
            case "SW":
                newLat = latitude - (mDesiredDistance * .8);
                newLng = longitude - (mDesiredDistance * .8);
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
        decimalFormat.setMaximumFractionDigits(8);
        decimalFormat.format(newLat);
        decimalFormat.format(newLng);

        yelpLatLng[0] = newLat;
        yelpLatLng[1] = newLng;

        return yelpLatLng;
    }

    private void yelpIt (final double latitude, final double longitude) {
        final String searchTerm;
        if (mSearchTermEditText.getText().toString().equals("")){
            searchTerm = "restaurants";
        } else {
            searchTerm = mSearchTermEditText.getText().toString();
        }

        Thread yelp = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] args = new String[4];
                args[0] = "--term";
                args[1] = "" + searchTerm;
                args[2] = "--ll";
                args[3] = latitude + " , " + longitude;

                YelpAPI.main(args, mContext);
            }
        });
        yelp.start();
    }

}
