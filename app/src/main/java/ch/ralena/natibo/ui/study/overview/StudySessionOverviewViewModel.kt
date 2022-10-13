package ch.ralena.natibo.ui.study.overview

import ch.ralena.natibo.data.NatiboResult
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.model.NatiboSession
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.usecases.data.FetchSessionWithSentencesUseCase
import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StudySessionOverviewViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
	private val languageRepository: LanguageRepository,
	private val fetchSessionWithSentencesUseCase: FetchSessionWithSentencesUseCase,
	private val dispatchers: DispatcherProvider
) : BaseViewModel<StudySessionOverviewViewModel.Listener>() {
	interface Listener {
		fun sessionLoaded(
			session: NatiboSession,
			nativeLanguage: LanguageRoom?,
			targetLanguage: LanguageRoom?
		)
	}

	private val coroutineScope = CoroutineScope(Dispatchers.IO)
	lateinit var session: NatiboSession

	fun getSentences(courseId: Long) {
		coroutineScope.launch {
			val result = courseRepository.fetchCourse(courseId)
			when (result) {
				is NatiboResult.Success -> fetchSentencesFromCourse(result.data)
				else -> Unit
			}

		}
	}

	private suspend fun fetchSentencesFromCourse(course: CourseRoom) {
		val nativeLanguage = languageRepository.fetchLanguage(course.nativeLanguageId)
		val targetLanguage =
			course.targetLanguageId?.let { languageRepository.fetchLanguage(it) }
		val session = fetchSessionWithSentencesUseCase.fetchSessionWithSentences(course.sessionId)
		if (session != null) {
			withContext(dispatchers.main()) {
				listeners.forEach {
					it.sessionLoaded(session, nativeLanguage, targetLanguage)
				}
			}
		}
	}
}