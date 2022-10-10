package ch.ralena.natibo.ui.settings_course.views

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.settings.CourseSettings
import ch.ralena.natibo.settings.views.IntSettingView
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CourseSettings(settings: CourseSettings, courseState: StateFlow<CourseRoom?>) {
	val course by courseState.collectAsState(initial = null)
	if (course != null) {
		Column {
			Text(text = "Settings for: ${course?.title}")
			IntSettingView(
				setting = settings.delayBetweenSentences,
				labelResId = R.string.settings_course_sentence_delay_label
			)
		}
	} else {
		Text(text = "Error loading course, press back and try again.")
	}
}
