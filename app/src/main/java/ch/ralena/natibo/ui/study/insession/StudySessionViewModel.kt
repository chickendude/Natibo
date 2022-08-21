package ch.ralena.natibo.ui.study.insession

import androidx.annotation.StringRes
import ch.ralena.natibo.R
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.PackRepository
import ch.ralena.natibo.data.room.SentenceRepository
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.data.room.`object`.SessionRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.*
import javax.inject.Inject

class StudySessionViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
	private val packRepository: PackRepository,
	private val sentenceRepository: SentenceRepository,
	private val sessionRepository: SessionRepository,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<StudySessionViewModel.Listener>() {
	interface Listener {
		fun makeToast(@StringRes stringRes: Int)
		fun onCourseLoaded(course: CourseRoom)
		fun onCourseNotFound(@StringRes errorMsgRes: Int?)
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	fun settingsIconClicked() {
		listeners.forEach { it.makeToast(R.string.course_settings_not_implemented) }
	}

	fun fetchCourse(id: Long) {
		coroutineScope.launch(dispatcherProvider.main()) {
			val result = withContext(dispatcherProvider.io()) {
				courseRepository.fetchCourse(id)
			}
			when (result) {
				is Result.Success -> loadCourse(result.data)
				is Result.Failure -> listeners.forEach { it.onCourseNotFound(result.stringRes) }
			}
		}
	}

	// region Helper functions ---------------------------------------------------------------------
	private suspend fun loadCourse(course: CourseRoom) {
		// TODO: Remove
		if (courseRepository.countSessions(course.id) > 0)
			sessionRepository.deleteAll()

		if (courseRepository.countSessions(course.id) == 0)
			createSession(course)
		listeners.forEach { it.onCourseLoaded(course) }
	}

	private suspend fun createSession(course: CourseRoom) {
		val numSessions = courseRepository.countSessions(course.id)
		val session = SessionRoom(
			index = numSessions + 1,
			progress = 0,
			courseId = course.id,
			sentenceIndices = getSentenceIndices(course)
		)
		val sessionId = sessionRepository.createSession(session)
//		courseRepository.updateCourse(course.copy(sessionId = sessionId))
	}

	private suspend fun getSentenceIndices(course: CourseRoom): String {
		val schedule = course.schedule
		val startingIndex = schedule.curSentenceIndex

		// First time sentences are seen they should be in order
		val initialSentences = mutableListOf<Int>()
		for (i in 0..schedule.numSentences) {
			initialSentences.add(startingIndex + i)
		}

		// Review of new sentences should be randomized
		val sentences = mutableListOf<Int>()
		val numTimes = schedule.reviewPattern.split(' ').first().toInt()
		for (i in 0..schedule.numSentences) {
			repeat(numTimes - 1) { sentences.add(startingIndex + i) }
		}
		sentences.shuffle()

		// make sure no sentences are repeated twice in a row
		sentences.forEachIndexed { i, sentence ->
			if (i > 0) {
				if (sentences[i - 1] == sentence) {
					sentences.removeAt(i)
					for (index in 1..sentences.size) {
						if (sentences[index] != sentence &&	sentences[index -1] != sentence)
							sentences.add(index, sentence)
					}
				}
			}
		}

		return initialSentences.joinToString { "," } + sentences.joinToString { "," }
	}
// endregion Helper functions ------------------------------------------------------------------
}