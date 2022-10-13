package ch.ralena.natibo.ui.study.insession.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.ralena.natibo.R
import ch.ralena.natibo.ui.study.insession.StudySessionManager
import ch.ralena.natibo.ui.study.insession.StudyState

@Composable
internal fun PlayPause(studySessionManager: StudySessionManager) {
	val studyState = studySessionManager.studyState().collectAsState()
	val image =
		if (studyState.value == StudyState.PLAYING) R.drawable.ic_pause else R.drawable.ic_play
	Row(modifier = Modifier.padding(5.dp)) {
		Arrow(
			modifier = Modifier.weight(1f),
			text = "<",
			onClick = { studySessionManager.previousSentencePair() }
		)
		Image(
			painter = painterResource(id = image),
			contentDescription = "Play/Pause",
			contentScale = ContentScale.FillWidth,
			modifier = Modifier
				.width(180.dp)
				.fillMaxHeight()
				.clickable(
					onClick = { studySessionManager.togglePausePlay() },
					interactionSource = remember { MutableInteractionSource() },
					indication = null
				)
		)
		Arrow(
			modifier = Modifier.weight(1f),
			text = ">",
			onClick = { studySessionManager.nextSentencePair() }
		)
	}
}

@Composable
internal fun Arrow(text: String, modifier: Modifier, onClick: () -> Unit) {
	Box(modifier = modifier
		.clickable { onClick() }
		.fillMaxHeight(),
		contentAlignment = Alignment.Center
	) {
		Text(
			textAlign = TextAlign.Center,
			fontSize = 30.sp,
			text = text
		)
	}
}