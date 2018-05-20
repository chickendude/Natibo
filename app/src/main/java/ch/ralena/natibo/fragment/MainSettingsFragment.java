package ch.ralena.natibo.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import ch.ralena.natibo.R;

public class MainSettingsFragment extends PreferenceFragmentCompat {
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.main_settings);
	}
}
