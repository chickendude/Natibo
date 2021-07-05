package ch.ralena.natibo.ui.course.detail

import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.ScreenNavigator
import kotlinx.coroutines.*
import javax.inject.Inject

class CourseDetailViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
	private val languageRepository: LanguageRepository,
	private val screenNavigator: ScreenNavigator,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<CourseDetailViewModel.Listener>() {
	interface Listener {
		fun onCourseFetched(course: CourseRoom)
		fun onLanguageFetched(language: LanguageRoom)
		fun onCourseNotFound()
		fun onSessionStarted()
		fun onSessionNotStarted()
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	private var course: CourseRoom? = null

	fun fetchCourse(id: Long) {
		coroutineScope.launch {
			val result = courseRepository.fetchCourse(id)
			if (result is Result.Success)
				fetchCourseSuccess(result.data)
			else
				withContext(dispatcherProvider.main()) {
					listeners.forEach { it.onCourseNotFound() }
				}
		}
	}

	fun fetchLanguage(id: Long) {
		coroutineScope.launch {
			languageRepository.fetchLanguage(id)?.let { language ->
				withContext(dispatcherProvider.main()) {
					listeners.forEach { it.onLanguageFetched(language) }
				}
			}
		}
	}

	fun startSession() {
		course?.let { screenNavigator.toStudySessionFragment(it.id) }
	}

	fun deleteCourse() {
		coroutineScope.launch(dispatcherProvider.io()) {
			course?.let { courseRepository.deleteCourse(it) }
		}
		screenNavigator.toCourseListFragment()
	}

	fun openSettings() {
		course?.let { screenNavigator.toCourseSettingsFragment(it.id) }
	}

	// region Helper functions----------------------------------------------------------------------
	private fun fetchCourseSuccess(course: CourseRoom) {
		this.course = course
		for (l in listeners) l.onCourseFetched(course)
		if (course.sessionId == 0L)
			for (l in listeners) l.onSessionNotStarted()
		else
			for (l in listeners) l.onSessionStarted()
	}
// endregion Helper functions-------------------------------------------------------------------
}