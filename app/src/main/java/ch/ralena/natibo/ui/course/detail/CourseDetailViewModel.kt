package ch.ralena.natibo.ui.course.detail

import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.ui.base.BaseViewModel
import javax.inject.Inject

class CourseDetailViewModel @Inject constructor(
	private val courseRepository: CourseRepository
) : BaseViewModel<CourseDetailViewModel.Listener>() {
	interface Listener {
		fun onCourseFetched(course: Course)
	}

	fun fetchCourse(courseId: String) {
		courseRepository.fetchCourse(courseId) {
			if (it is Result.Success)
				for (l in listeners)
					l.onCourseFetched(it.data)
		}
	}
}