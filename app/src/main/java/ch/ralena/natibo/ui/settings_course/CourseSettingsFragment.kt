package ch.ralena.natibo.ui.settings_course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import ch.ralena.natibo.R
import ch.ralena.natibo.data.NatiboResult
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.settings.CourseSettings
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.settings_course.views.CourseSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CourseSettingsFragment : Fragment() {
	@Inject
	lateinit var courseRepository: CourseRepository

	@Inject
	lateinit var courseSettings: CourseSettings

	private var course: CourseRoom? = null

	//	private val sharedPreferenceChangeListener =
//		SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
//			when (key) {
//				PREF_PAUSE -> realm!!.executeTransaction { r: Realm? ->
//					course.setPauseMillis(
//						sharedPreferences.getString(PREF_PAUSE, "1000")!!.toInt()
//					)
//				}
//				PREF_PLAYBACK_SPEED -> realm!!.executeTransaction { r: Realm? ->
//					val speed = sharedPreferences.getString(
//						getString(R.string.playback_speed_key),
//						getString(R.string.playback_speed_default)
//					)!!
//						.toFloat()
//					course.setPlaybackSpeed(speed)
//				}
//			}
//		}
//
	fun onPreferenceTreeClick(preference: Preference): Boolean {
		when (preference.key) {
			PREF_START -> loadCourseBookListFragment()
		}
		return true
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		(requireActivity() as MainActivity).enableBackButton()

		val courseId = requireArguments().getLong(KEY_ID)
		val courseState = MutableStateFlow<CourseRoom?>(null)
		lifecycleScope.launch {
			val result = courseRepository.fetchCourse(courseId)
			when (result) {
				is NatiboResult.Success -> courseState.value = result.data
				else -> Unit
			}
		}

		return ComposeView(requireContext()).apply {
			setContent {
				CourseSettings(courseSettings, courseState)
			}
		}
	}

	override fun onStart() {
		super.onStart()
		requireActivity().title = getString(R.string.settings)
	}

//	fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String) {
//		// unregister SharedPreferencesChangedListener so that we don't unnecessarily trigger
//		// it when we load the default value from the course
//		prefs = preferenceManager.sharedPreferences
//
//		// load preferences from course into our shared preferences
////		prefs.edit()
////			.putString(PREF_PAUSE, course.getPauseMillis().toString() + "")
////			.putString(PREF_PLAYBACK_SPEED, course.getPlaybackSpeed().toString() + "")
////			.apply()
//		addPreferencesFromResource(R.xml.course_settings)
//
//		// check if you should be able to choose the starting sentence or not
//		val start = findPreference(PREF_START)
//		start.isEnabled = course.getPacks().size > 0
//		val playbackSpeed = findPreference(PREF_PLAYBACK_SPEED) as EditTextPreference
//		playbackSpeed.onPreferenceChangeListener =
//			Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
//				val newSpeed: Float = newValue as kotlin.String?. toFloat ()
//				// Set speed to the allowed range between 0.5 and 2.5 and round to one decimal place
//				var speed = (newSpeed * 10).toInt() / 10f
//				speed = MathUtils.clamp(speed, PLAYBACK_MIN_SPEED, PLAYBACK_MAX_SPEED)
//				if (newSpeed != speed) {
//					playbackSpeed.text = speed.toString() + ""
//					return@OnPreferenceChangeListener false
//				}
//				true
//			}
//	}

	private fun loadCourseBookListFragment() {
		val fragment = CoursePickSentenceFragment()

		// attach course id to bundle
		val bundle = Bundle()
		bundle.putLong(CoursePickSentenceFragment.TAG_COURSE_ID, course?.id ?: -1)
		fragment.arguments = bundle

		// load fragment
		requireFragmentManager()
			.beginTransaction()
			.replace(R.id.fragmentPlaceHolder, fragment)
			.addToBackStack(null)
			.commit()
	}

	companion object {
		val TAG = CourseSettingsFragment::class.java.simpleName
		const val PREF_PAUSE = "pref_pause"
		const val PREF_START = "pref_start"
		const val PREF_PLAYBACK_SPEED = "pref_playback_speed"
		const val KEY_ID = "key_id"
		private const val PLAYBACK_MIN_SPEED = 0.5f
		private const val PLAYBACK_MAX_SPEED = 2.5f
	}
}


