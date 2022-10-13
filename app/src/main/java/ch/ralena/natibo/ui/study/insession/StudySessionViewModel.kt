package ch.ralena.natibo.ui.study.insession

import androidx.annotation.StringRes
import ch.ralena.natibo.R
import ch.ralena.natibo.data.NatiboResult
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.SessionRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.usecases.data.CreateSessionUseCase
import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StudySessionViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
	private val sessionRepository: SessionRepository,
	private val createSessionUseCase: CreateSessionUseCase,
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
				is NatiboResult.Success -> loadCourse(result.data)
				is NatiboResult.Failure -> listeners.forEach { it.onCourseNotFound(result.stringRes) }
			}
		}
	}

	// region Helper functions ---------------------------------------------------------------------
	private suspend fun loadCourse(course: CourseRoom) {
		createSessionUseCase.createSessionIfNecessary(course)
		listeners.forEach { it.onCourseLoaded(course) }
	}

// endregion Helper functions ------------------------------------------------------------------
}