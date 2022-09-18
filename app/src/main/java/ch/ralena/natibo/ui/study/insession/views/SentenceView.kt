package ch.ralena.natibo.ui.study.insession.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.model.NatiboSession
import ch.ralena.natibo.ui.study.insession.StudySessionManager
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun Sentences(studySessionManager: StudySessionManager) {
	val sentence = studySessionManager.currentSentence().collectAsState().value ?: return
	val session = studySessionManager.session
	Column(modifier = Modifier.padding(5.dp)) {
		Row {
			Column {
				Text(
					modifier = Modifier.padding(5.dp),
					text = (session.currentSentenceIndex + 1).toString()
				)
				Text(
					modifier = Modifier.padding(5.dp),
					text = sentence.native.index.toString()
				)
			}
			Column {
				Text(text = sentence.native.original, modifier = Modifier.padding(5.dp))
				if (sentence.native.ipa.isNotEmpty())
					Text(text = sentence.native.ipa, modifier = Modifier.padding(5.dp))
				if (sentence.native.romanization.isNotEmpty())
					Text(text = sentence.native.romanization, modifier = Modifier.padding(5.dp))
			}
		}

		Divider(color = Color.Black, modifier = Modifier.fillMaxWidth())

		sentence.target?.let {
			Text(text = it.original, modifier = Modifier.padding(5.dp))
			if (it.ipa.isNotEmpty())
				Text(text = it.ipa, modifier = Modifier.padding(5.dp))
			if (it.romanization.isNotEmpty())
				Text(text = it.romanization, modifier = Modifier.padding(5.dp))
		}
	}
}