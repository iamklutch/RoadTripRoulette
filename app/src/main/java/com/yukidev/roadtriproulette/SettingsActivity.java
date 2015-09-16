package com.yukidev.roadtriproulette;


import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;


public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String KEY_DIST = "pref_max_dist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        getSharedPreferences(KEY_DIST, 0)
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences(KEY_DIST, 0)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_DIST)) {
//            Preference preference = findPreference(key);
//            preference.setSummary(sharedPreferences.getString(key, ""));
//            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//            MainActivity activity = new MainActivity();
//            activity.setDefaultMaxDistance
//                    (Integer.parseInt(preferences.getString(KEY_DIST, "125")));
        }
    }
}