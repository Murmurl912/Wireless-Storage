package com.example.wirelessstorage.ui.setting;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.wirelessstorage.R;

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}