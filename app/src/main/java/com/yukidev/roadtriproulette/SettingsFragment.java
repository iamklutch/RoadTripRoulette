package com.yukidev.roadtriproulette;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by James on 9/16/2015.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
