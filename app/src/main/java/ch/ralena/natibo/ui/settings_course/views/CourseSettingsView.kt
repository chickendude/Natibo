package ch.ralena.natibo.ui.settings_course.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.ralena.natibo.R
import ch.ralena.natibo.settings.CourseSettings
import ch.ralena.natibo.settings.views.IntSettingView

@Composable
fun CourseSettingsView(settings: CourseSettings, onNavigateToSentencePick: () -> Unit) {
	Column(
		modifier = Modifier.padding(8.dp)
	) {
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
