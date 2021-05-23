package ch.ralena.natibo.ui.study.insession

import androidx.annotation.StringRes
import ch.ralena.natibo.R
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.ui.base.BaseViewModel
import io.realm.Realm
import javax.inject.Inject

class StudySessionViewModel @Inject constructor(
	private val courseRepository: CourseRepository
) :	BaseViewModel<StudySessionViewModel.Listener>() {
	interface Listener {
		fun makeToast(@StringRes stringRes: Int)
		fun onCourseLoaded(course: Course)
		fun onCourseNotFound(@StringRes errorMsgRes: Int?)
	}

	fun settingsIconClicked() {
		listeners.forEach { it.makeToast(R.string.course_settings_not_implemented) }
	}

	fun fetchCourse(id: String) {
		courseRepository.fetchCourse(id) { result ->
			when (result) {
				is Result.Success -> loadCourse(result.data)
				is Result.Failure -> listeners.forEach { it.onCourseNotFound(result.stringRes) }
			}
		}
	}

	private fun loadCourse(course: Course) {
		if (course.currentDay == null || course.currentDay.isCompleted)
			courseRepository.prepareNextDay(course)
		listeners.forEach { it.onCourseLoaded(course) }
	}
}