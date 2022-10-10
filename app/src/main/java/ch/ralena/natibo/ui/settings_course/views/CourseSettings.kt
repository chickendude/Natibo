package ch.ralena.natibo.ui.settings_course.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.settings.CourseSettings
import ch.ralena.natibo.settings.views.IntSettingView
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CourseSettingsView(settings: CourseSettings) {
		Column {
			Text(text = "Settings for: ${settings.course.title}")
			IntSettingView(
				setting = settings.delayBetweenSentences,
				labelResId = R.string.settings_course_sentence_delay_label
			)
			Text(text = "Select sentence",
				modifier = Modifier.clickable {

				})
		}
}
