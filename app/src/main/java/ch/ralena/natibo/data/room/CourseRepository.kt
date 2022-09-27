package ch.ralena.natibo.data.room

import ch.ralena.natibo.R
import ch.ralena.natibo.data.NatiboResult
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.ScheduleRoom
import ch.ralena.natibo.data.room.dao.CourseDao
import io.realm.Realm
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

	// region Old Realm stuff
//	/**
//	 * Prepares the sentences for the next day of study.
//	 *
//	 * @param course The course to prepare
//	 */
//	fun prepareNextDay(course: Course) {
//		// add current day to past days
//
//		// add current day to past days
//		if (course.currentDay?.isCompleted == true)
//			realm.executeTransaction { course.pastDays.add(course.currentDay) }
//
//		// create a new day
//		realm.executeTransaction { r: Realm ->
//			val day = r.createObject(Day::class.java, UUID.randomUUID().toString())
//
//			// add the sentence sets from the current day to the next day
//			if (course.currentDay != null && course.currentDay.isCompleted) {
//				day.sentenceSets.addAll(course.currentDay.sentenceSets)
//
//				// move yesterday's new words to the front of the reviews
//				val lastSet = day.sentenceSets.last()
//				day.sentenceSets.remove(lastSet)
//				day.sentenceSets.add(0, lastSet)
//			}
//			val reviewPattern: RealmList<Int> = course.schedule.getReviewPattern()
//			val numSentences: Int = course.schedule.numSentences
//			val sentenceIndex: Int = course.schedule.sentenceIndex
//			course.schedule.sentenceIndex = sentenceIndex + numSentences
//
//			// create new set of sentences based off the schedule
//			val sentenceSet = SentenceSet()
//			sentenceSet.sentenceSet =
//				getSentenceGroups(sentenceIndex, numSentences, course.languages, course.packs)
//			sentenceSet.reviews = reviewPattern
//			sentenceSet.isFirstDay = true
//			sentenceSet.order = course.schedule.order
//
//			// add sentence set to list of sentencesets for the next day's studies
//			day.sentenceSets.add(sentenceSet)
//			day.isCompleted = false
//			day.pauseMillis = course.pauseMillis
//			day.setPlaybackSpeed(course.playbackSpeed)
//			course.currentDay = day
//		}
//		val emptySentenceSets: MutableList<SentenceSet> = ArrayList()
//		for (set in course.currentDay.sentenceSets) {
//			// create sentence set and mark it to be deleted if it is empty
//			if (!set.buildSentences(realm)) {
//				emptySentenceSets.add(set)
//			}
//		}
//
//		// delete the sentence sets with no reviews left
//		realm.executeTransaction {
//			course.currentDay.sentenceSets.removeAll(emptySentenceSets)
//		}
//	}
	// endregion
}