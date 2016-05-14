package com.team10.codeflow;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;

/**
 * Created by Federico on 13/05/2016.
 */
public class SettingsMenu extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);

        getFragmentManager().beginTransaction()
                .add(R.id.settings_mainArea, new PrefFragment())
                .commit();


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Log.d("PREFCHANGE", key + " changed!");
        Log.d("PREFCHANGE", key + " changed to " + sharedPreferences.getString(key, ""));


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", "Settings onDestroy() called");
    }

    public void startMainMenuScreen(View view) {        //this view parameter is the one that was clicked (the button)
        finish();
    }



}

