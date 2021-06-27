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
	private fun loadCourse(course: CourseRoom) {
		if (course.sessionId == 0L)
			createSession(course)
		listeners.forEach { it.onCourseLoaded(course) }
	}

	private fun createSession(course: CourseRoom) {
		coroutineScope.launch {
			val numSessions = courseRepository.countSessions(course.id)
			val session = SessionRoom(
				index = numSessions + 1,
				progress = 0,
				courseId = course.id
			)
			val sessionId = sessionRepository.createSession(session)
			addSentencesToSession(course, sessionId)
//			courseRepository.updateCourse(course.copy(sessionId = sessionId))
		}
	}

	private suspend fun addSentencesToSession(course: CourseRoom, sessionId: Long) {
		var curSentenceIndex = course.schedule.curSentenceIndex
		val order = course.schedule.order
		val reviewPattern = course.schedule.reviewPattern
		val numSentences = course.schedule.numSentences

		val sentenceSets = ArrayList<List<SentenceRoom>>()
//		val packSentences = sentenceRepository.fetchSentencesInPack(course.packName)
		val packs = packRepository.fetchPackWithSentencesByNameAndLanguages(course.packName, listOf(course.baseLanguageId, course.targetLanguageId))
		val newSentences = ArrayList<SentenceRoom>()
		reviewPattern.forEach { numTimesStr: Char ->
			val numTimes = Character.getNumericValue(numTimesStr)

		}

//		sessionRepository.addSentencesToSession(sessionId, )
	}
	// endregion Helper functions ------------------------------------------------------------------
}