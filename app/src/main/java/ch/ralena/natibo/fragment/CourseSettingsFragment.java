package ch.ralena.natibo.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.math.MathUtils;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import ch.ralena.natibo.MainActivity;
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
	public static final String PREF_PLAYBACK_SPEED = "pref_playback_speed";
	public static final String KEY_ID = "key_id";

	private static final float PLAYBACK_MIN_SPEED = 0.5f;
	private static final float PLAYBACK_MAX_SPEED = 2.5f;

	private SharedPreferences prefs;
	private Realm realm;
	private Course course;

	private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			switch (key) {
				case PREF_PAUSE:
					realm.executeTransaction(r -> course.setPauseMillis(Integer.parseInt(sharedPreferences.getString(PREF_PAUSE, "1000"))));
					break;
				case PREF_PLAYBACK_SPEED:
					realm.executeTransaction(r -> {
						float speed = Float.parseFloat(sharedPreferences.getString(getString(R.string.playback_speed_key), getString(R.string.playback_speed_default)));
						course.setPlaybackSpeed(speed);
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
	public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
		((MainActivity) getActivity()).enableBackButton();

		// load course from fragment arguments
		String courseId = getArguments().getString(KEY_ID);
		realm = Realm.getDefaultInstance();
		course = realm.where(Course.class).equalTo("id", courseId).findFirst();

		// unregister SharedPreferencesChangedListener so that we don't unnecessarily trigger
		// it when we load the default value from the course
		prefs = getPreferenceManager().getSharedPreferences();
		unregisterChangeListener();

		// load preferences from course into our shared preferences
		prefs.edit()
				.putString(PREF_PAUSE, course.getPauseMillis() + "")
				.putString(PREF_PLAYBACK_SPEED, course.getPlaybackSpeed() + "")
				.apply();
		registerChangeListener();

		addPreferencesFromResource(R.xml.course_settings);

		// check if you should be able to choose the starting sentence or not
		Preference start = findPreference(PREF_START);
		start.setEnabled(course.getPacks().size() > 0);

		EditTextPreference playbackSpeed = (EditTextPreference) findPreference(PREF_PLAYBACK_SPEED);
		playbackSpeed.setOnPreferenceChangeListener((preference, newValue) -> {
			float newSpeed = Float.parseFloat((String) newValue);
			// Set speed to the allowed range between 0.5 and 2.5 and round to one decimal place
			float speed = ((int) (newSpeed * 10)) / 10f;
			speed = MathUtils.clamp(speed, PLAYBACK_MIN_SPEED, PLAYBACK_MAX_SPEED);
			if (newSpeed != speed) {
				playbackSpeed.setText(speed + "");
				return false;
			}
			return true;
		});
	}

	@Override
	public void onStop() {
		super.onStop();
		unregisterChangeListener();
	}

	private void loadCourseBookListFragment() {
		CoursePickSentenceFragment fragment = new CoursePickSentenceFragment();

		// attach course id to bundle
		Bundle bundle = new Bundle();
		bundle.putString(CoursePickSentenceFragment.TAG_COURSE_ID, course.getId());
		fragment.setArguments(bundle);

		// load fragment
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.addToBackStack(null)
				.commit();
	}

	private void registerChangeListener() {
		prefs.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
	}

	private void unregisterChangeListener() {
		prefs.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
	}
}
