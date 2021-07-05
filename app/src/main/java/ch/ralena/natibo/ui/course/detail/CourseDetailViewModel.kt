package ch.ralena.natibo.ui.course.detail

import androidx.annotation.StringRes
import ch.ralena.natibo.R
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.PackRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.ScreenNavigator
import kotlinx.coroutines.*
import javax.inject.Inject

class CourseDetailViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
	private val packRepository: PackRepository,
	private val languageRepository: LanguageRepository,
	private val screenNavigator: ScreenNavigator,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<CourseDetailViewModel.Listener>() {
	interface Listener {
		fun onCourseFetched(course: CourseRoom)
		fun onCourseNotFound()
		fun onLanguageFetched(language: LanguageRoom)
		fun onPackFetched(pack: PackRoom)
		fun onPackNotFound()
		fun updateSessionStatusText(@StringRes msgId: Int)
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	private var course: CourseRoom? = null

	fun fetchData(id: Long) {
		coroutineScope.launch {
			val result = courseRepository.fetchCourse(id)
			if (result is Result.Success<CourseRoom>) {
				val course = result.data
				// Notify that the course was found
				fetchCourseSuccess(course)
				// Fetch the pack from the database
				fetchPack(course.packId)
			} else
				withContext(dispatcherProvider.main()) {
					listeners.forEach { it.onCourseNotFound() }
				}
		}
		coroutineScope.launch {
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
		listeners.forEach { it.onCourseFetched(course) }
		val msgId =
			if (course.sessionId == 0L) R.string.start_session else R.string.continue_session
		listeners.forEach { it.updateSessionStatusText(msgId) }
	}

	private suspend fun fetchPack(packId: Long) {
		val pack = packRepository.fetchPack(packId)

		// If the pack was found, notify listeners. If it wasn't found, notify of error on the main
		// thread.
		pack?.let {
			listeners.forEach { it.onPackFetched(pack) }
		} ?: run {
			withContext(dispatcherProvider.main()) {
				listeners.forEach { it.onPackNotFound() }
			}
		}
	}
// endregion Helper functions-------------------------------------------------------------------
}