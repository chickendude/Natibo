package ch.ralena.natibo.ui.course.list

import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.ScreenNavigator
import io.realm.RealmResults
import javax.inject.Inject

class CourseListViewModel @Inject constructor(
		private val courseRepository: CourseRepository,
		private val screenNavigator: ScreenNavigator
) : BaseViewModel<CourseListViewModel.Listener>() {
	interface Listener {
		fun showCourses(courses: List<Course>)
		fun showNoCourses()
	}

	fun fetchCourses() {
		val courses = courseRepository.fetchCourses()
		if (courses.size > 0)
			for (listener in listeners)
				listener.showCourses(courses)
		else
			for (listener in listeners)
				listener.showNoCourses()
	}

	fun fabClicked() {
		screenNavigator.toCourseCreateFragment()
	}

	fun redirectToCourseDetail(courseId: String) {
		screenNavigator.toCourseDetailFragment(courseId)
	}
}