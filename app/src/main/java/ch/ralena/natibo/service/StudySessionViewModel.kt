package ch.ralena.natibo.service

import android.util.Log
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.model.NatiboSession
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.usecases.data.FetchSessionWithSentencesUseCase
import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val TAG = StudySessionViewModel::class.simpleName

class StudySessionViewModel @Inject constructor(
	private val fetchSessionWithSentencesUseCase: FetchSessionWithSentencesUseCase,
	private val courseRepository: CourseRepository,
	private val sessionRepository: SessionRepository,
	dispatcherProvider: DispatcherProvider
) {
	private val job = Job()
	val coroutineScope = CoroutineScope(job + dispatcherProvider.default())

	private val readyToStart = MutableStateFlow(false)
	fun readyToStart() = readyToStart.asStateFlow()

	private lateinit var session: NatiboSession

	fun start(courseId: Long) {
		coroutineScope.launch {
			val result = courseRepository.fetchCourse(courseId)
			when (result) {
				is Result.Success -> loadSession(result.data)
				else -> Log.e(TAG, "Unable to load course with id $courseId")
			}
		}
	}

	fun nextSentence(): NatiboSentence? {
		session.currentSentenceIndex += 1
		val sentence = session.sentences.getOrNull(session.currentSentenceIndex)
		return sentence
	}

	private suspend fun loadSession(course: CourseRoom) {
		session =
			fetchSessionWithSentencesUseCase.fetchSessionWithSentences(course.sessionId) ?: return
		Log.d("----", session.toString())
		readyToStart.value = true
	}
}