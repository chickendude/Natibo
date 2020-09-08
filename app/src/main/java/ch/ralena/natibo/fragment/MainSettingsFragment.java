package ch.ralena.natibo.fragment;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import ch.ralena.natibo.R;

public class MainSettingsFragment extends PreferenceFragmentCompat {
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.main_settings);
	}
}
