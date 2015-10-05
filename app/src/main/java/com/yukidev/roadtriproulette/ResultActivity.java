package com.yukidev.roadtriproulette;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResultActivity extends AppCompatActivity implements View.OnTouchListener {

    @Bind(R.id.resultBusinessNameTextView)TextView mBusinessName;
    @Bind(R.id.navigateButton)ImageButton mNavigateButton;
    @Bind(R.id.noWayButton)ImageButton mNoWayButton;
    @Bind(R.id.businessImageView)ImageView mBusinessImageView;
    @Bind(R.id.starsImageButton)ImageButton mStarsImageView;
    @Bind(R.id.directionTextView) TextView mCityTextView;

    private String mName;
    private double mLat;
    private double mLng;
    private double mRating;
    private String mImageUrl;
    private String mRatingUrl;
    private String mCity;
    private String mYelpUrl;
    private SharedPreferences mPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);
        PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false);

        //Make the ads
        AdView mAdView = (AdView) findViewById(R.id.adViewResult);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("C4A083AE232F9FB04BFA58FBF0E57A0A")
                .build();
        mAdView.loadAd(adRequest);

        mBusinessName.setTypeface(null, Typeface.BOLD);
        mCityTextView.setTypeface(null, Typeface.BOLD);


        Intent intent = getIntent();
        mName = intent.getStringExtra("name");
        mLat = intent.getDoubleExtra("lat", 47);
        mLng = intent.getDoubleExtra("lng", -110);
        mImageUrl = intent.getStringExtra("imageUrl");
        mRating = intent.getDoubleExtra("rating", 3.0);
        mRatingUrl = intent.getStringExtra("ratingUrl");
        mCity = intent.getStringExtra("city");
        mYelpUrl = intent.getStringExtra("yelpUrl");


        if (mRating == 0.0){
            mStarsImageView.setImageResource(R.drawable.nostars);
        } else if (mRating == 1.0){
            mStarsImageView.setImageResource(R.drawable.onestar);
        } else if (mRating == 1.5){
            mStarsImageView.setImageResource(R.drawable.onehalfstars);
        } else if (mRating == 2.0){
            mStarsImageView.setImageResource(R.drawable.twostars);
        } else if (mRating == 2.5){
            mStarsImageView.setImageResource(R.drawable.twohalfstars);
        } else if (mRating == 3.0){
            mStarsImageView.setImageResource(R.drawable.threestars);
        } else if (mRating == 3.5){
            mStarsImageView.setImageResource(R.drawable.threehalfstars);
        } else if (mRating == 4.0){
            mStarsImageView.setImageResource(R.drawable.fourstars);
        } else if (mRating == 4.5){
            mStarsImageView.setImageResource(R.drawable.fourhalfstars);
        } else if (mRating == 5.0){
            mStarsImageView.setImageResource(R.drawable.fivestars);
        }

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean maskData = mPreferences.getBoolean(SettingsActivity.KEY_MASK_DATA, false);
        if (maskData) {
            mBusinessName.setOnTouchListener(this);
            mCityTextView.setOnTouchListener(this);
        } else {
            mBusinessName.setText(mName);
            mCityTextView.setText(mCity);
        }

            Picasso.with (this)
                    .load(mImageUrl)
                    .placeholder(R.drawable.ic_action_photo_dark)
                    .into(mBusinessImageView);
    }

    @OnClick (R.id.noWayButton)
    public void goBack(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @OnClick (R.id.navigateButton)
    public void navigate(){
        Intent naviIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + mLat +"," + mLng));
        naviIntent.setPackage("com.google.android.apps.maps");
        startActivity(naviIntent);
    }

    @OnClick (R.id.starsImageButton)
    public void gotoYelpBusinessPage(){
        Intent yelpIntent = new Intent(Intent.ACTION_VIEW);
        yelpIntent.setData(Uri.parse(mYelpUrl));
        // Checks for the yelp app, if not, then browser
        if (isYelpAppAvailable()){
            yelpIntent.setPackage("com.yelp.android");
        }
        if (yelpIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(yelpIntent);
        }

    }

    public Boolean isYelpAppAvailable(){
        PackageManager pm = this.getPackageManager();
        try {
            pm.getPackageInfo("com.yelp.android", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
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
    public boolean onTouch(View v, MotionEvent event) {
        mBusinessName.setText(mName);
        mCityTextView.setText(mCity);
        return false;
    }
}
