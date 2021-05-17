package ch.ralena.natibo.ui.course.detail

import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.ScreenNavigator
import javax.inject.Inject

class CourseDetailViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
	private val screenNavigator: ScreenNavigator
) : BaseViewModel<CourseDetailViewModel.Listener>() {
	interface Listener {
		fun onCourseFetched(course: Course)
		fun onCourseNotFound()
		fun noPacksSelected()
		fun onSessionStarted()
		fun onSessionNotStarted()
	}

	lateinit var courseId: String

	fun fetchCourse(courseId: String?) {
		if (courseId == null) {
			for (l in listeners) l.onCourseNotFound()
		} else {
			courseRepository.fetchCourse(courseId) {
				if (it is Result.Success)
					fetchCourseSuccess(it.data)
				else
					for (l in listeners) l.onCourseNotFound()
			}
		}
	}

	suspend fun addRemovePack(packId: String) {
		courseRepository.togglePackInCourse(packId, courseId)
	}

	fun startSession() {
		courseRepository.fetchCourse(courseId) {
			if (it is Result.Success) {
				val course = it.data
				if (course.packs.size == 0)
					for (l in listeners)
						l.noPacksSelected()
				else
					screenNavigator.toStudySessionFragment(courseId)
			}
		}
	}

	fun deleteCourse() {
		courseRepository.deleteCourse(courseId)
		screenNavigator.toCourseListFragment()
	}

	fun openSettings() {
		screenNavigator.toCourseSettingsFragment(courseId)
	}

	// region Helper functions----------------------------------------------------------------------
	private fun fetchCourseSuccess(course: Course) {
		this.courseId = course.id
		for (l in listeners) l.onCourseFetched(course)
		if (course.currentDay == null)
			for (l in listeners) l.onSessionNotStarted()
		else
			for (l in listeners) l.onSessionStarted()
	}
	// endregion Helper functions-------------------------------------------------------------------
}