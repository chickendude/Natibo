package ch.ralena.natibo.service

import android.util.Log
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.model.NatiboSession
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.usecases.data.FetchSessionWithSentencesUseCase
import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val TAG = StudySessionViewModel::class.simpleName

internal class StudySessionViewModel @Inject constructor(
	private val fetchSessionWithSentencesUseCase: FetchSessionWithSentencesUseCase,
	private val courseRepository: CourseRepository,
	private val sessionRepository: SessionRepository,
	dispatcherProvider: DispatcherProvider
) {
	private val job = Job()
	val coroutineScope = CoroutineScope(job + dispatcherProvider.default())

	private val events = MutableSharedFlow<Event>()
	fun events() = events.asSharedFlow()

	private var _currentSentence: NatiboSentence? = null
	val currentSentence get() = _currentSentence

	lateinit var session: NatiboSession

	fun start(courseId: Long) {
		coroutineScope.launch {
			val result = courseRepository.fetchCourse(courseId)
			when (result) {
				is Result.Success -> loadSession(result.data)
				else -> Log.e(TAG, "Unable to load course with id $courseId")
			}
		}
	}

	fun nextSentence() {
		coroutineScope.launch {
			session.nextSentence()
			_currentSentence = session.currentSentencePair
			val sentence = session.currentSentence
			if (sentence == null) events.emit(Event.SessionFinished)
			else events.emit(Event.SentenceLoaded(sentence))
		}
	}

	private suspend fun loadSession(course: CourseRoom) {
		session =
			fetchSessionWithSentencesUseCase.fetchSessionWithSentences(course.sessionId) ?: return
		events.emit(Event.SessionLoaded)
		nextSentence()
	}

	internal sealed class Event {
		data class SentenceLoaded(val sentence: SentenceRoom) : Event()
		object SessionLoaded : Event()
		object SessionFinished : Event()
	}
}