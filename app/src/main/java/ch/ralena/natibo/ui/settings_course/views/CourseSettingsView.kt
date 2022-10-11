package ch.ralena.natibo.ui.settings_course.views

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import ch.ralena.natibo.R
import ch.ralena.natibo.settings.CourseSettings
import ch.ralena.natibo.settings.views.IntSettingView

@Composable
fun CourseSettingsView(settings: CourseSettings, onNavigateToSentencePick: () -> Unit) {
	Column {
		Text(text = "Settings for: ${settings.course.title}")
		IntSettingView(
			setting = settings.delayBetweenSentences,
			labelResId = R.string.settings_course_sentence_delay_label
		)
		TextButton(onClick = onNavigateToSentencePick) {
			Text(text = "Set first new sentence")
		}
	}
}
