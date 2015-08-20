package com.yukidev.roadtriproulette;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResultActivity extends AppCompatActivity {

    @Bind(R.id.resultBusinessNameTextView)TextView mBusinessName;
    @Bind(R.id.resultBusinessLat)TextView mBusinessLat;
    @Bind(R.id.resultBusinessLng)TextView mBusinessLng;
    @Bind(R.id.navigateButton)Button mNavigateButton;
    @Bind(R.id.businessImageView)ImageView mBusinessImageView;
    @Bind(R.id.starsImageView)ImageView mStarsImageView;

    private String mName;
    private double mLat;
    private double mLng;
    private double mRating;
    private String mImageUrl;
    private String mRatingUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        mName = intent.getStringExtra("name");
        mLat = intent.getDoubleExtra("lat", 47);
        mLng = intent.getDoubleExtra("lng", -110);
        mImageUrl = intent.getStringExtra("imageUrl");
        mRating = intent.getDoubleExtra("rating", 3.0);
        mRatingUrl = intent.getStringExtra("ratingUrl");

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

        mBusinessName.setText(mName);
        mBusinessLat.setText("" + mLat);
        mBusinessLng.setText("" + mLng);


            Picasso.with (this)
                    .load(mImageUrl)
                    .placeholder(R.drawable.ic_action_photo_dark)
                    .into(mBusinessImageView);

//            Picasso.with(this)
//                    .load(mRatingUrl)
//                    .placeholder(R.drawable.nostars)
//                    .into(mStarsImageView);

    }

    @OnClick (R.id.navigateButton)
    public void navigate(){
        Intent naviIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + mLat +"," + mLng));
        naviIntent.setPackage("com.google.android.apps.maps");
        startActivity(naviIntent);
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

}
