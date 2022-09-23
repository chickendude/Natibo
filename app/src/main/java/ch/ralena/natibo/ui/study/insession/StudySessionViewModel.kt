package ch.ralena.natibo.ui.study.insession

import androidx.annotation.StringRes
import ch.ralena.natibo.R
import ch.ralena.natibo.data.NatiboResult
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.SessionRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StudySessionViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
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
				is NatiboResult.Success -> loadCourse(result.data)
				is NatiboResult.Failure -> listeners.forEach { it.onCourseNotFound(result.stringRes) }
			}
		}
	}

	// region Helper functions ---------------------------------------------------------------------
	private suspend fun loadCourse(course: CourseRoom) {
		// TODO: Remove
//		if (courseRepository.countSessions(course.id) > 0)
//			sessionRepository.deleteAll()

		// TODO: Create next session if previous session is finished
		val session = sessionRepository.fetchSession(course.sessionId)
		if (courseRepository.countSessions(course.id) == 0 ||
			session != null && session.progress > session.sentenceIndices.split(",").size
		) {
			createSession(course, session)
		}
		listeners.forEach { it.onCourseLoaded(course) }
	}

	private suspend fun createSession(course: CourseRoom, previousSession: SessionRoom?) {
		val numSessions = courseRepository.countSessions(course.id)
		val session = SessionRoom(
			index = numSessions + 1,
			progress = previousSession?.progress ?: 0,
			courseId = course.id,
			sentenceIndices = getSentenceIndices(course)
		)
		val sessionId = sessionRepository.createSession(session)
		courseRepository.updateCourse(course.copy(sessionId = sessionId))
	}

	private fun getSentenceIndices(course: CourseRoom): String {
		val schedule = course.schedule
		val startingIndex = schedule.curSentenceIndex

		// First time sentences are seen they should be in order
		val initialSentences = mutableListOf<Int>()
		for (i in 0 until schedule.numSentences) {
			initialSentences.add(startingIndex + i)
		}

		// Review of new sentences should be randomized
		val sentences = mutableListOf<Int>()
		val numTimes = schedule.reviewPattern.split(' ').first().toInt() - 1
		for (i in 0 until schedule.numSentences) {
			repeat(numTimes) { sentences.add(startingIndex + i) }
		}
		sentences.shuffle()
		sentences.addAll(0, initialSentences)

		// make sure no sentences are repeated twice in a row
		val repeatedSentences = mutableListOf<Int>()
		sentences.forEachIndexed { i, sentence ->
			if (i > 0 && sentences[i - 1] == sentence) {
				repeatedSentences.add(sentence)
				sentences[i] = -1
			}
		}
		sentences.removeAll { it == -1 }

		// Insert sentences back into list where they won't be repeated
		repeatedSentences.forEach { sentence ->
			var wasInserted = false
			for (index in 1 until sentences.size) {
				if (sentences[index] != sentence && sentences[index - 1] != sentence) {
					sentences.add(index, sentence)
					wasInserted = true
					break
				}
			}
			// If we couldn't find a place for it, just add it to the end.
			// Should only be an issue with really small sizes, e.g. 1-2 unique sentences
			if (!wasInserted) sentences.add(sentence)
		}

		return sentences.joinToString(separator = ",")
	}
// endregion Helper functions ------------------------------------------------------------------
}