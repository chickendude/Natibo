package ch.ralena.natibo.ui.settings_course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.ralena.natibo.R
import ch.ralena.natibo.data.NatiboResult
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.settings.CourseSettings
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.settings_course.views.CourseSettingsView
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

//		SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
//			when (key) {
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
				val course = courseState.collectAsState().value
				if (course != null) {
					courseSettings.course = course
					SettingsScreen(settings = courseSettings)
				} else {
					Text(text = "Error loading course, press back and try again.")
				}
			}
		}
	}

	override fun onStart() {
		super.onStart()
		requireActivity().title = getString(R.string.settings)
	}

	@Composable
	fun SettingsScreen(
		settings: CourseSettings,
		navController: NavHostController = rememberNavController()
	) {
		NavHost(navController = navController, startDestination = "home") {
			composable("home") {
				CourseSettingsView(
					settings = settings,
					onNavigateToSentencePick = { navController.navigate("pick_sentence") })
			}
			composable("pick_sentence") {
				SentencePick(course = settings.course, viewModel = hiltViewModel())
			}

		}
	}

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

	companion object {
		val TAG = CourseSettingsFragment::class.java.simpleName
		const val KEY_ID = "key_id"
		private const val PLAYBACK_MIN_SPEED = 0.5f
		private const val PLAYBACK_MAX_SPEED = 2.5f
	}
}


