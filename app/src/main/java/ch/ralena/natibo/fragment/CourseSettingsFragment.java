package ch.ralena.natibo.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

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
	public static final String KEY_ID = "key_id";

	private SharedPreferences prefs;
	private Realm realm;
	private Course course;

	private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(PREF_PAUSE)) {
				realm.executeTransaction(r -> {
					course.setPauseMillis(Integer.parseInt(sharedPreferences.getString(PREF_PAUSE, "1000")));
				});
			}
		}
	};

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
}
