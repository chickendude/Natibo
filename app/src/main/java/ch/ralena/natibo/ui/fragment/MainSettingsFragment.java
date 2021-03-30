package ch.ralena.natibo.ui.fragment;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import ch.ralena.natibo.R;

public class MainSettingsFragment extends PreferenceFragmentCompat {
	public static final String TAG = MainSettingsFragment.class.getSimpleName();

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.main_settings);
	}
}
