package ch.ralena.natibo.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import ch.ralena.natibo.R;
import ch.ralena.natibo.object.Course;
import io.realm.Realm;


/*
Settings:
1. Pause between sentences.
*/
public class CourseSettingsFragment extends PreferenceFragmentCompat {
	public static final String PREF_PAUSE = "pref_pause";
	public static final String PREF_START = "pref_start";
	public static final String KEY_ID = "key_id";

	private SharedPreferences prefs;
	private Realm realm;
	private Course course;

	private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			switch (key) {
				case PREF_PAUSE:
					realm.executeTransaction(r -> {
						course.setPauseMillis(Integer.parseInt(sharedPreferences.getString(PREF_PAUSE, "1000")));
					});
					break;
			}
		}
	};

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		switch (preference.getKey()) {
			case PREF_START:
				loadCourseBookListFragment();
				break;
		}
		return super.onPreferenceTreeClick(preference);
	}

	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle(getString(R.string.settings));
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
		// load course from fragment arguments
		String courseId = getArguments().getString(KEY_ID);
		realm = Realm.getDefaultInstance();
		course = realm.where(Course.class).equalTo("id", courseId).findFirst();

		// unregister SharedPreferencesChangedListener so that we don't unnecessarily trigger
		// it when we load the default value from the course
		prefs = getPreferenceManager().getSharedPreferences();
		prefs.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

		// load preferences from course into our shared preferences
		prefs.edit()
				.putString(PREF_PAUSE, course.getPauseMillis() + "")
				.apply();
		prefs.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

		addPreferencesFromResource(R.xml.course_settings);
	}

	@Override
	public void onStop() {
		super.onStop();
		prefs.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
	}

	private void loadCourseBookListFragment() {
		CourseBookListFragment fragment = new CourseBookListFragment();

		// attach course id to bundle
		Bundle bundle = new Bundle();
		bundle.putString(CourseBookListFragment.TAG_COURSE_ID, course.getId());
		fragment.setArguments(bundle);

		// load fragment
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.addToBackStack(null)
				.commit();
	}
}
