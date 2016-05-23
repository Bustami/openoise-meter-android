package it.piemonte.arpa.openoise;

import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

//    public static class SettingsFragment extends PreferenceFragment {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//            // Load the preferences from an XML resource
//            addPreferencesFromResource(R.xml.pref_general);
//        }
//    }
    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(it.piemonte.arpa.openoise.R.xml.pref_general);
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

//        public static final String key_time_display = "time_display";
//
//        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
//                                              String key) {
//            if (key.equals(key_time_display)) {
//                Preference connectionPref = findPreference(key);
//                // Set summary to be the user-description for the selected value
//                //connectionPref.setSummary(sharedPreferences.getString(key, ""));
//                connectionPref.setSummary("ciaociao");
//            }
//        }


        // forse questa parte serve per aggiornare il summary
        @Override
        public void onResume() {
            super.onResume();
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
                Preference preference = getPreferenceScreen().getPreference(i);
                if (preference instanceof PreferenceGroup) {
                    PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                    for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                        updatePreference(preferenceGroup.getPreference(j));
                    }
                } else {
                    updatePreference(preference);
                }
            }
        }
        // forse questa parte serve per aggiornare il summary
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePreference(findPreference(key));
        }

        private void updatePreference(Preference preference) {
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
//                String new_Summary = listPreference.getEntry().toString();
//                CharSequence new_Summary_cs = new_Summary;
//                listPreference.setSummary(new_Summary_cs);
                listPreference.setSummary(listPreference.getEntry());
            }

            if (preference instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) preference;
                preference.setSummary(editTextPref.getText() + " dB");
            }
        }

    }

}
