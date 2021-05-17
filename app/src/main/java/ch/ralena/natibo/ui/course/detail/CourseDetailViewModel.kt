package ch.ralena.natibo.ui.course.detail

import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Pack
import ch.ralena.natibo.ui.base.BaseViewModel
import javax.inject.Inject

class CourseDetailViewModel @Inject constructor(
	private val courseRepository: CourseRepository
) : BaseViewModel<CourseDetailViewModel.Listener>() {
	interface Listener {
		fun onCourseFetched(course: Course)
		fun onCourseNotFound()
	}

	lateinit var courseId: String

	fun fetchCourse(courseId: String?) {
		if (courseId == null)
			for (l in listeners) l.onCourseNotFound()
		else
			courseRepository.fetchCourse(courseId) {
				if (it is Result.Success) {
					this.courseId = it.data.id
					for (l in listeners) l.onCourseFetched(it.data)
				} else
					for (l in listeners) l.onCourseNotFound()
			}
	}

	fun refreshCourse() {
		fetchCourse(courseId)
	}

	suspend fun addRemovePack(packId: String) {
		courseRepository.togglePackInCourse(packId, courseId)
	}
}