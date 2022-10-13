package ch.ralena.natibo.data.room

import ch.ralena.natibo.R
import ch.ralena.natibo.data.NatiboResult
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.ScheduleRoom
import ch.ralena.natibo.data.room.dao.CourseDao
import javax.inject.Inject

/**
 * Repository for obtaining [CourseRoom] data.
 */
class CourseRepository @Inject constructor(
	private val courseDao: CourseDao
) {
	/**
	 * Fetches all courses in the database.
	 *
	 * @return [List] containing all courses in the database.
	 */
	suspend fun fetchCourses(): List<CourseRoom> = courseDao.getAll()

	suspend fun createCourse(
		order: String,
		numSentencesPerDay: Int,
		startingSentence: Int,
		dailyReviews: List<String>,
		title: String,
		baseLanguageId: Long,
		targetLanguageId: Long?,
		packId: Long
	): Long {
		val scheduleRoom = ScheduleRoom(
			numSentencesPerDay,
			startingSentence,
			order,
			dailyReviews.joinToString(" ")
		)
		val courseRoom =
			CourseRoom(title, baseLanguageId, targetLanguageId, packId, scheduleRoom, 0)
		return courseDao.insert(courseRoom)
	}

	/**
	 * Fetches a single course.
	 *
	 * This fetches a single course and returns [NatiboResult.Success] if it was successful, otherwise
	 * it returns [NatiboResult.Failure].
	 *
	 * @param courseId The ID of the course to look for.
	 * @return [NatiboResult.Success] if it was successful, otherwise [NatiboResult.Failure].
	 */
	suspend fun fetchCourse(courseId: Long): NatiboResult<CourseRoom> {
		val course = courseDao.getCourseById(courseId)
		return if (course == null)
			NatiboResult.Failure(R.string.course_not_found)
		else
			NatiboResult.Success(course)
	}

	suspend fun deleteCourse(course: CourseRoom) {
		courseDao.delete(course)
	}

	suspend fun updateCourse(course: CourseRoom) {
		courseDao.update(course)
	}

	suspend fun countSessions(courseId: Long): Int = courseDao.getSessionCount(courseId)
}