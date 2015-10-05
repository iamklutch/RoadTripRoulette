package com.yukidev.roadtriproulette;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private String mSearchTerm;

    private double mCurrentLongitude;
    private double mCurrentLatitude;
    private double mYelpLat;
    private double mYelpLng;
    private double mDesiredDistance;
    private String mDesiredDirection;
    private AnimationDrawable mFrameAnimation;
    private int mEggIncrement;
    private int mDefaultMaxDistance;
    private String[] mDefaultSearchCategories;
    private SharedPreferences mPreferences;

    Context mContext;

    private Location lastKnownLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Bind (R.id.searchTermEditText)EditText mSearchTermEditText;
    @Bind (R.id.letsGoImageButton)ImageButton mLetsGoButton;
    @Bind (R.id.directionImageButton)ImageButton mDirectionImageButton;
    @Bind (R.id.spinningWheelImageView)ImageView mSpinningWheelView;
    @Bind (R.id.randomButton)ImageView mRandomButton;
    @Bind (R.id.northButton)ImageView mNorthButton;
    @Bind (R.id.northEastButton)ImageView mNorthEastButton;
    @Bind (R.id.eastButton)ImageView mEastButton;
    @Bind (R.id.southEastButton)ImageView mSouthEastButton;
    @Bind (R.id.southButton)ImageView mSouthButton;
    @Bind (R.id.southWestButton)ImageView mSouthWestButton;
    @Bind (R.id.westButton)ImageView mWestButton;
    @Bind (R.id.northWestButton)ImageView mNorthWestButton;
    @Bind (R.id.doNotPressButton)ImageView mDoNotPressButton;
    @Bind (R.id.settingsButton)ImageButton mSettingsButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false);

        mEggIncrement = 0;

        Intent intent = getIntent();
        try {
            if (intent.getStringExtra("error").equals("noYelp")){
                Toast.makeText(this, "Yelp! is not available in this area"
                        , Toast.LENGTH_LONG).show();
            } else if (intent.getStringExtra("error").equals("noBusiness")){
                Toast.makeText(this, "No locations found, try another direction or search term"
                        , Toast.LENGTH_LONG).show();
            }
        } catch (NullPointerException npe) {
            // do nothing
        }

        mContext = this;

        //Make the ads
        AdView mAdView = (AdView) findViewById(R.id.adViewFront);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("C4A083AE232F9FB04BFA58FBF0E57A0A")
                .build();
        mAdView.loadAd(adRequest);

        mSpinningWheelView.setVisibility(View.INVISIBLE);

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

                mSpinningWheelView.setVisibility(View.VISIBLE);
                mSpinningWheelView.setBackgroundResource(R.drawable.spin_animation);
                mFrameAnimation = (AnimationDrawable) mSpinningWheelView.getBackground();
                mFrameAnimation.setVisible(true, true);
                mFrameAnimation.start();

                if (isNetworkAvailable()) {

                    if (mSearchTermEditText.getText().toString().equals("")){
                        randomSearch();
                    } else {
                        mSearchTerm = mSearchTermEditText.getText().toString();
                    }
                    getYelpLatLng(mCurrentLatitude, mCurrentLongitude, mDesiredDirection);
                    yelpIt(mYelpLat, mYelpLng);


                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.network_unavailable),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void randomSearch(){

        // this sets the default search categories for the randomCategory
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // the string[] is to set the default on first load for the Set<String> categories
        String[] catArray = getResources().getStringArray(R.array.pref_default_selected_category);
        Set<String> defaultCatSet = new HashSet<>(Arrays.asList(catArray));
        Set<String> categories = mPreferences.getStringSet(SettingsActivity.KEY_CATEGORIES, defaultCatSet);
        mDefaultSearchCategories = new String[categories.size()];
        mDefaultSearchCategories = categories.toArray(new String[] {});

        Random random = new Random();
        int i = random.nextInt(mDefaultSearchCategories.length);
        mSearchTerm = mDefaultSearchCategories[i];

    }

    @OnClick (R.id.settingsButton)
    protected void goToSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @OnClick (R.id.distanceImageButton)
    protected void getDesiredDistance(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText distanceVariable = new EditText(this);

        distanceVariable.setInputType(InputType.TYPE_CLASS_NUMBER);
        distanceVariable.setHint("Distance, in miles . . ");
        builder.setTitle("How far?");
        builder.setMessage("How far do you want to drive?");
        builder.setCancelable(true);
        builder.setView(distanceVariable);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (distanceVariable.equals("")) {
                    randomDistance();

                }
                // put what they chose as distance
                int dist = Integer.parseInt(distanceVariable.getText().toString());
                mDesiredDistance = milesToLatLng(dist);
                Toast.makeText(MainActivity.this, "Distance set: " + dist + " miles.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNeutralButton("Random", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                randomDistance();
            }
        });
        builder.create().show();


    }

    private void randomDistance(){
        Random distRandom = new Random();
        int dist = distRandom.nextInt(mDefaultMaxDistance);
        mDesiredDistance = milesToLatLng(dist);
        Toast.makeText(this, "Distance: RANDOM up to " + mDefaultMaxDistance, Toast.LENGTH_SHORT).show();
    }

    @OnClick (R.id.directionImageButton)
    protected void directionButton(){
        mRandomButton.setVisibility(View.VISIBLE);
        mNorthButton.setVisibility(View.VISIBLE);
        mNorthEastButton.setVisibility(View.VISIBLE);
        mEastButton.setVisibility(View.VISIBLE);
        mSouthEastButton.setVisibility(View.VISIBLE);
        mSouthButton.setVisibility(View.VISIBLE);
        mSouthWestButton.setVisibility(View.VISIBLE);
        mWestButton.setVisibility(View.VISIBLE);
        mNorthWestButton.setVisibility(View.VISIBLE);
    }

    @OnClick (R.id.randomButton)
    protected void randomButton(){
        mDirectionImageButton.setImageResource(R.drawable.rtr_direction_random_button);
        mDesiredDirection = randomDirection();
        hideDirectionButtons();
    }

    @OnClick (R.id.northButton)
    protected void northButton(){
        mDesiredDirection = "N";
        mDirectionImageButton.setImageResource(R.drawable.rtr_direction_north_button);
        hideDirectionButtons();
    }

    @OnClick (R.id.northEastButton)
    protected void northEastButton(){
        mDesiredDirection = "NE";
        mDirectionImageButton.setImageResource(R.drawable.rtr_direction_northeast_button);
        hideDirectionButtons();
    }

    @OnClick (R.id.eastButton)
    protected void eastButton(){
        mDesiredDirection = "E";
        mDirectionImageButton.setImageResource(R.drawable.rtr_direction_east_button);
        hideDirectionButtons();
    }

    @OnClick (R.id.southEastButton)
    protected void southEastButton(){
        mDesiredDirection = "SE";
        mDirectionImageButton.setImageResource(R.drawable.rtr_direction_southeast_button);
        hideDirectionButtons();
    }

    @OnClick (R.id.southButton)
    protected void southButton(){
        mDesiredDirection = "S";
        mDirectionImageButton.setImageResource(R.drawable.rtr_direction_south_button);
        hideDirectionButtons();
    }

    @OnClick (R.id.southWestButton)
    protected void southWestButton(){
        mDesiredDirection = "SW";
        mDirectionImageButton.setImageResource(R.drawable.rtr_direction_southwest_button);
        hideDirectionButtons();
    }

    @OnClick (R.id.westButton)
    protected void westButton(){
        mDesiredDirection = "W";
        mDirectionImageButton.setImageResource(R.drawable.rtr_direction_west_button);
        hideDirectionButtons();
    }

    @OnClick (R.id.northWestButton)
    protected void northWestButton(){
        mDesiredDirection = "NW";
        mDirectionImageButton.setImageResource(R.drawable.rtr_direction_northwest_button);
        hideDirectionButtons();
    }

    private void hideDirectionButtons(){
        mRandomButton.setVisibility(View.INVISIBLE);
        mNorthButton.setVisibility(View.INVISIBLE);
        mNorthEastButton.setVisibility(View.INVISIBLE);
        mEastButton.setVisibility(View.INVISIBLE);
        mSouthEastButton.setVisibility(View.INVISIBLE);
        mSouthButton.setVisibility(View.INVISIBLE);
        mSouthWestButton.setVisibility(View.INVISIBLE);
        mWestButton.setVisibility(View.INVISIBLE);
        mNorthWestButton.setVisibility(View.INVISIBLE);
    }

    @OnClick (R.id.doNotPressButton)
    protected void doNotPressButton(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        if (mSearchTermEditText.getText().toString().equals("")){
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("Sorry, the button says do NOT press.  Did you not see that?" +
                    "  Besides, you don't even have anything in the search box");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement == 0){
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("Did you really just press the button?");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement == 1){
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("Why do you insist on defying my very simple command? And you\'re " +
                    "still not correct on the search term.");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement == 2){
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("You are rather persistent, aren\'t you.");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement == 3) {
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("I have the strangest feeling, like this has happened before." +
                    "  Do you believe in Deja Vu?");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement == 4) {
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("I have the strangest feeling, like this has happened before." +
                    "  Do you believe in Deja Vu?");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement > 4
                && mEggIncrement <= 5) {
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("This is getting rather boring.  Your term is still not correct.  Perhaps" +
                    " if you tapped the ad, something will happen. . . ");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement == 6) {
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("Ok, so the ad did nothing.  Well, I\'m out of ideas. . . ");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement >= 7
                && mEggIncrement <= 10) {
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("Sheesh! Like I said - I\'m out of ideas!");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement >= 11
                && mEggIncrement <= 15) {
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("Are you really still at this?");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement >= 16
                && mEggIncrement <= 20) {
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("Isn't your finger getting sore from all this tapping?");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement >= 21
                && mEggIncrement <= 35) {
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("tap tap tap tap tap tap tap tap tap tap TAP TAP TAP TAP TAP TAAAAPPPPPPP!");
            dialog.setCancelable(true);
        }else if (!mSearchTermEditText.getText().toString().equals("Klutch") && mEggIncrement >= 36) {
            mEggIncrement++;
            dialog.setTitle("Incorrect Search Term");
            dialog.setMessage("OK, OK!!! You win!!!!! the correct search term is \"Klutch\"!!!  Now " +
                    "PLEASE stop pressing the button!!!  Well, one more time wouldn\'t hurt since" +
                    " you know the correct search term.");
            dialog.setCancelable(true);
        }else if (mSearchTermEditText.getText().toString().equals("Klutch")) {
            mEggIncrement++;
            dialog.setTitle("Correct Search Term!");
            dialog.setMessage("You did it!! Congratulations!! I hope you didn't have to tap that" +
                    " stupid button too many times.");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Show me what I won!!!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(MainActivity.this, EasterEggActivity.class);
                    startActivity(intent);
                }
            });
        }

        dialog.show();
    }

    @Override
    protected void onResume(){
        super.onResume();

        // this sets the maximum distance selectable by the randomDistance method
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mDefaultMaxDistance = Integer
                .parseInt(mPreferences.getString(SettingsActivity.KEY_DIST, "125"));
        // this sets the default search categories for the randomCategory
        try {
            String[] catArray = getResources().getStringArray(R.array.pref_default_selected_category);
            Set<String> defaultCatSet = new HashSet<>(Arrays.asList(catArray));
            Set<String> categories = mPreferences.getStringSet(SettingsActivity.KEY_CATEGORIES, defaultCatSet);
            mDefaultSearchCategories = new String[categories.size()];
            mDefaultSearchCategories = categories.toArray(new String[] {});
        } catch (NullPointerException e){

        }


        // in case they don't choose direction . . .
        mDesiredDirection = randomDirection();
        // in case they didn't choose a distance. . .
        randomDistance();

        mGoogleApiClient.connect();
        try {
            if (mFrameAnimation.isRunning()) {
                mFrameAnimation.stop();
                mSpinningWheelView.setVisibility(View.INVISIBLE);
            }
        } catch (NullPointerException e) {
            // do nothing
        }

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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
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

    private String randomDirection() {
        String desiredDirection = "S";
        Random random = new Random(8);
        int i = random.nextInt();
        switch (i){
            case 0:
                desiredDirection = "N";
                break;
            case 1:
                desiredDirection = "NE";
                break;
            case 2:
                desiredDirection = "E";
                break;
            case 3:
                desiredDirection = "SE";
                break;
            case 4:
                desiredDirection = "S";
                break;
            case 5:
                desiredDirection = "SW";
                break;
            case 6:
                desiredDirection = "W";
                break;
            case 7:
                desiredDirection = "NW";
                break;
        }
        return desiredDirection;
    }



    private void getYelpLatLng(double latitude, double longitude, String direction) {
        DecimalFormat decimalFormat = new DecimalFormat();
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

        mYelpLat = newLat;
        mYelpLng = newLng;

    }

    private void yelpIt (final double latitude, final double longitude) {
        final String searchTerm = mSearchTerm;

        try {
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
            yelp.setContextClassLoader(getClass().getClassLoader());
            yelp.start();
        } catch (Exception el) {
            Log.e("YelpIt :" + el.getMessage(), "");
        }
    }

}
