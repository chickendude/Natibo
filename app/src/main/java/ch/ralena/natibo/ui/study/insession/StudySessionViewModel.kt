package ch.ralena.natibo.ui.study.insession

import androidx.annotation.StringRes
import ch.ralena.natibo.R
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import io.realm.Realm
import kotlinx.coroutines.*
import javax.inject.Inject

class StudySessionViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<StudySessionViewModel.Listener>() {
	interface Listener {
		fun makeToast(@StringRes stringRes: Int)
		fun onCourseLoaded(course: CourseRoom)
		fun onCourseNotFound(@StringRes errorMsgRes: Int?)
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	fun settingsIconClicked() {
		listeners.forEach { it.makeToast(R.string.course_settings_not_implemented) }
	}

	fun fetchCourse(id: Long) {
		coroutineScope.launch(dispatcherProvider.main()) {
			val result = withContext(dispatcherProvider.io()) {
				courseRepository.fetchCourse(id)
			}
			when (result) {
				is Result.Success -> loadCourse(result.data)
				is Result.Failure -> listeners.forEach { it.onCourseNotFound(result.stringRes) }
			}
		}
	}

	private fun loadCourse(course: CourseRoom) {
//	if (course.session == null || course.session.isCompleted)
//		courseRepository.prepareNextDay(course)
		listeners.forEach { it.onCourseLoaded(course) }
	}
}