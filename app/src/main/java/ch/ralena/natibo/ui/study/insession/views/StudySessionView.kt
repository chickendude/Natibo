package ch.ralena.natibo.ui.study.insession.views

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ch.ralena.natibo.service.StudySessionServiceKt
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.study.insession.StudySessionViewModel

@Composable
internal fun StudySession(
//	viewModel: StudySessionViewModel = hiltViewModel()
) {
	Column() {
//		Sentences(currentSentence = service.currentSentence(), session = service.viewModel.session)
//		PlayPause(service = service)
	}
}

private fun connectToService(context: Context) {
	val serviceIntent = Intent(context, StudySessionServiceKt::class.java)
	ContextCompat.startForegroundService(context, serviceIntent)
}
private fun disconnectFromService(context: Context) {
	val serviceIntent = Intent(context, StudySessionServiceKt::class.java)
	context.stopService(serviceIntent)
}
