package com.yukidev.roadtriproulette;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by James on 9/16/2015.
 */
public class SettingsFragment extends PreferenceFragment {

    private static final String KEY_DIST = "pref_max_dist";
    private SharedPreferences mPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String value = mPreferences.getString(KEY_DIST, "Custom Distance");
        Preference preference = findPreference(KEY_DIST);
        preference.setSummary("Current setting: " + value + " miles");

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged
                            (SharedPreferences sharedPreferences, String key) {

                        String value = sharedPreferences.getString(KEY_DIST, "");
                        Preference preference = findPreference(KEY_DIST);
                        preference.setSummary("Current setting: " + value + " miles");
                    }
                };
        //this is after the listener to empty garbage collection in sharedPrefs
        mPreferences.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPreferences.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
    }
}
