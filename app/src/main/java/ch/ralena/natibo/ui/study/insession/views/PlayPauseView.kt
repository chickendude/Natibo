package ch.ralena.natibo.ui.study.insession.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.ralena.natibo.R
import ch.ralena.natibo.ui.study.insession.StudySessionManager
import ch.ralena.natibo.ui.study.insession.StudyState

@Composable
internal fun PlayPause(studySessionManager: StudySessionManager) {
	val studyState = studySessionManager.studyState().collectAsState()
	val image =
		if (studyState.value == StudyState.PLAYING) R.drawable.ic_pause else R.drawable.ic_play
	Column(modifier = Modifier.padding(5.dp)) {
		Image(
			painter = painterResource(id = image),
			contentDescription = "Play/Pause",
			contentScale = ContentScale.FillWidth,
			modifier = Modifier
				.fillMaxSize()
				.clickable { studySessionManager.togglePausePlay() }
		)
	}
}