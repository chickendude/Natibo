package ch.ralena.natibo.ui.study.overview

import ch.ralena.natibo.data.NatiboResult
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.SentenceRepository
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.model.NatiboSession
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.usecases.data.FetchSessionWithSentencesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class StudySessionOverviewViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
	private val languageRepository: LanguageRepository,
	private val fetchSessionWithSentencesUseCase: FetchSessionWithSentencesUseCase,
) : BaseViewModel<StudySessionOverviewViewModel.Listener>() {
	interface Listener {
		fun sessionLoaded(session: NatiboSession, languages: Pair<LanguageRoom?, LanguageRoom?>)
	}

	private var course: CourseRoom? = null
	private val coroutineScope = CoroutineScope(Dispatchers.IO)
	lateinit var session: NatiboSession

	fun getSentences(courseId: Long) {
		coroutineScope.launch {
			val result = courseRepository.fetchCourse(courseId)
			if (result is NatiboResult.Success) {
				course = result.data
				val nativeLanguage = languageRepository.fetchLanguage(course!!.nativeLanguageId)
				val targetLanguage =
					course?.targetLanguageId?.let { languageRepository.fetchLanguage(it) }
				fetchSessionWithSentencesUseCase.fetchSessionWithSentences(result.data.sessionId)
					?.let { session ->
						listeners.forEach { it.sessionLoaded(session, Pair(nativeLanguage, targetLanguage)) }
					}
			}

		}
	}
}