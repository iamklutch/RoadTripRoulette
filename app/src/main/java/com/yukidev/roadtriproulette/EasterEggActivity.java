package com.yukidev.roadtriproulette;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EasterEggActivity extends AppCompatActivity {

    protected double mLat;
    protected double mLng;

    @Bind(R.id.winBannerImageView) ImageView mWinBanner;
    @Bind(R.id.winBannerImageButton)ImageButton mWinButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easter_egg);
        ButterKnife.bind(this);
    }

    @OnClick (R.id.winBannerImageButton)
    protected void navigate(){
        Toast.makeText(this, "navigate to the coolest places on earth.",Toast.LENGTH_LONG).show();
        whereTo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_easter_egg, menu);
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

    private void whereTo(){
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);

        switch(month){
            case 0:
                mLat = 37.201076;
                mLng = -112.988718;
                break;
            case 1:
                mLat = 37.201076;
                mLng = -112.988718;
                break;
            case 2:
                mLat = 44.417281;
                mLng = -110.598120;
                break;
            case 3:
                mLat = 44.417281;
                mLng = -110.598120;
                break;
            case 4:
                mLat = 44.417281;
                mLng = -110.598120;
                break;
            case 5:
                mLat = 44.417281;
                mLng = -110.598120;
                break;
            case 6:
                mLat = 40.822376;
                mLng = -110.800705;
                break;
            case 7:
                mLat = 40.822376;
                mLng = -110.800705;
                break;
            case 8:
                mLat = 44.417281;
                mLng = -110.598120;
                break;
            case 9:
                mLat = 44.417281;
                mLng = -110.598120;
                break;
            case 10:
                mLat = 37.201076;
                mLng = -112.988718;
                break;
            case 11:
                mLat = 37.201076;
                mLng = -112.988718;
                break;
            default:
                mLat = 37.201076;
                mLng = -112.988718;
                break;
        }

        Intent naviIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + mLat + "," + mLng));
        naviIntent.setPackage("com.google.android.apps.maps");
        startActivity(naviIntent);
    }
}
