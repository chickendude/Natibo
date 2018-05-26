package ch.ralena.natibo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

import ch.ralena.natibo.R;


/*
Settings:
1. Pause between sentences.
*/
public class CourseSettingsFragment extends PreferenceFragmentCompatDividers {
	@Override
	public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.course_settings);
	}
}
