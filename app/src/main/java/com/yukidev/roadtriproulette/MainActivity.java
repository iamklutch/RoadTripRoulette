package com.yukidev.roadtriproulette;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.*;
import android.os.Process;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private double longitude;
    private double latitude;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        LocationManager locationManager = (LocationManager)
//                this.getSystemService(Context.LOCATION_SERVICE);
//
//        String locationProvider = LocationManager.NETWORK_PROVIDER;
//        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
//        longitude = lastKnownLocation.getLongitude();
//        latitude = lastKnownLocation.getLatitude();

        Thread yelp = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] args = new String[4];
                args[0] = "--term";
                args[1] = "tacos";
                args[2] = "--ll";
                args[3] = "41.2156 , -111.97";

                YelpAPI.main(args);
            }
        });

        yelp.start();
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

}
