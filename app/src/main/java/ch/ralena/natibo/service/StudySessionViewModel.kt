package ch.ralena.natibo.service

import android.util.Log
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class StudySessionViewModel @Inject constructor(
	private val sessionRepository: SessionRepository,
	private val dispatcherProvider: DispatcherProvider
) {
	val job = Job()
	val coroutineScope = CoroutineScope(job + dispatcherProvider.default())

	fun start(sessionId: Long) {
		coroutineScope.launch {
			val session = sessionRepository.fetchSessionWithSentencesById(sessionId)
			Log.d("----", session.toString())
		}
	}

}