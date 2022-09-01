package ch.ralena.natibo.service

import android.util.Log
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

private val TAG = StudySessionViewModel::class.simpleName

class StudySessionViewModel @Inject constructor(
	private val courseRepository: CourseRepository,
	private val sessionRepository: SessionRepository,
	dispatcherProvider: DispatcherProvider
) {
	private val job = Job()
	val coroutineScope = CoroutineScope(job + dispatcherProvider.default())

	fun start(courseId: Long) {
		coroutineScope.launch {
			val result = courseRepository.fetchCourse(courseId)
			when(result) {
				is Result.Success -> loadSession(result.data)
				else -> Log.e(TAG, "Unable to load course with id $courseId")
			}
		}
	}

	private suspend fun loadSession(course: CourseRoom) {
		val session = sessionRepository.fetchSessionWithSentencesById(course.sessionId)
		Log.d("----", session.toString())
	}

}