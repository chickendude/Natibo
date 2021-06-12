package ch.ralena.natibo.ui.course.detail

import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.ui.base.BaseListener
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.ScreenNavigator
import kotlinx.coroutines.*
import javax.inject.Inject

class CourseDetailViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
	private val screenNavigator: ScreenNavigator,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<CourseDetailViewModel.Listener>() {
	interface Listener {
		fun onCourseFetched(course: CourseRoom)
		fun onCourseNotFound()
		fun noPacksSelected()
		fun onSessionStarted()
		fun onSessionNotStarted()
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	private var course: CourseRoom? = null

	fun fetchCourse(courseId: Long) {
		coroutineScope.launch {
			val result = courseRepository.fetchCourse(courseId)
			if (result is Result.Success)
				fetchCourseSuccess(result.data)
			else
				for (l in listeners) l.onCourseNotFound()
		}
	}

	suspend fun addRemovePack(packId: String) {
		course?.let { courseRepository.togglePackInCourse(packId, it.id) }
	}

	fun startSession() {
//			if (course?.packs.size == 0)
//				for (l in listeners)
//					l.noPacksSelected()
//			else
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
		if (course.session == null)
			for (l in listeners) l.onSessionNotStarted()
		else
			for (l in listeners) l.onSessionStarted()
	}
// endregion Helper functions-------------------------------------------------------------------
}