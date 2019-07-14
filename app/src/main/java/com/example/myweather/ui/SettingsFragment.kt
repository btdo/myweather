package com.example.myweather.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.myweather.R


/**
 * Settings fragment to inflate Preferences
 *
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
