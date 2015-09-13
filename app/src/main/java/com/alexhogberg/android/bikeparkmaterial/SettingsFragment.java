package com.alexhogberg.android.bikeparkmaterial;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.alexhogberg.android.R;

/**
 * Created by Alexander on 2015-09-13.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}