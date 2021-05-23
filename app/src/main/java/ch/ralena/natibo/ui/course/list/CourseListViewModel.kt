package ch.ralena.natibo.ui.course.list

import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.ScreenNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CourseListViewModel @Inject constructor(
		private val courseRepository: CourseRepository,
		private val screenNavigator: ScreenNavigator
) : BaseViewModel<CourseListViewModel.Listener>() {
	interface Listener {
		fun showCourses(courses: List<CourseRoom>)
		fun showNoCourses()
	}

	fun fetchCourses() {
		coroutineScope.launch(Dispatchers.Main) {
			val courses = withContext(Dispatchers.IO) {
				courseRepository.fetchCourses()
			}
			if (courses.isNotEmpty())
				for (listener in listeners)
					listener.showCourses(courses)
			else
				for (listener in listeners)
					listener.showNoCourses()
		}
	}

	fun fabClicked() {
		screenNavigator.toCourseCreateFragment()
	}

	fun redirectToCourseDetail(courseId: Int) {
		screenNavigator.toCourseDetailFragment(courseId)
	}
}