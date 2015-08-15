package com.yukidev.roadtriproulette;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResultActivity extends AppCompatActivity {

    @Bind(R.id.resultBusinessNameTextView)TextView mBusinessName;
    @Bind(R.id.resultBusinessLat)TextView mBusinessLat;
    @Bind(R.id.resultBusinessLng)TextView mBusinessLng;
    @Bind(R.id.navigateButton)Button mNavigateButton;
    private String mName;
    private double mLat;
    private double mLng;
    private String mImageUrl;

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

        mBusinessName.setText(mName);
        mBusinessLat.setText("" + mLat);
        mBusinessLng.setText("" + mLng);

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
