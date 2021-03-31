package ch.ralena.natibo.ui.course.list

import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.ui.base.BaseViewModel
import io.realm.RealmResults
import javax.inject.Inject

class CourseListViewModel @Inject constructor(
		private val courseRepository: CourseRepository
) : BaseViewModel<CourseListViewModel.Listener>() {
	interface Listener {
		fun showCourses(courses: RealmResults<Course>)
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
}