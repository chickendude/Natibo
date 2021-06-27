package ch.ralena.natibo.ui.course.list

import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.ScreenNavigator
import kotlinx.coroutines.*
import javax.inject.Inject

class CourseListViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
	private val languageRepository: LanguageRepository,
	private val screenNavigator: ScreenNavigator,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<CourseListViewModel.Listener>() {
	interface Listener {
		fun showCourses(courses: List<CourseRoom>, languages: List<LanguageRoom>)
		fun showNoCourses()
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	fun fetchCourses() {
		coroutineScope.launch(dispatcherProvider.main()) {
			val courses = async(dispatcherProvider.io()) {
				courseRepository.fetchCourses()
			}
			val languages = async(dispatcherProvider.io()) {
				languageRepository.fetchLanguages()
			}
			if (courses.await().isNotEmpty() && languages.await().isNotEmpty())
				for (listener in listeners)
					listener.showCourses(courses.await(), languages.await())
			else
				for (listener in listeners)
					listener.showNoCourses()
		}
	}

	fun fabClicked() {
		screenNavigator.toCourseCreateFragment()
	}

	fun redirectToCourseDetail(courseId: Long) {
		screenNavigator.toCourseDetailFragment(courseId)
	}
}