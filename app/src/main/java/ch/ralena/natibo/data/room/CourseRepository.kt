package ch.ralena.natibo.data.room

import ch.ralena.natibo.R
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.Pack
import ch.ralena.natibo.data.room.`object`.Schedule
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.executeTransactionAwait
import java.util.*
import javax.inject.Inject

/**
 * Repository for obtaining [Course] data.
 */
class CourseRepository @Inject constructor(private val realm: Realm) {
	/**
	 * Fetches all courses in the database.
	 *
	 * @return `RealmResults` containing all courses in the database.
	 */
	fun fetchCourses(): RealmResults<Course> = realm.where(Course::class.java).findAll()

	fun createCourse(
		order: String,
		numSentencesPerDay: Int,
		startingSentence: Int,
		dailyReviews: List<String>,
		title: String,
		languages: List<Language>
	): Course {
		// --- begin transaction
		realm.beginTransaction()

		// create sentence schedule
		val schedule: Schedule =
			realm.createObject(Schedule::class.java, UUID.randomUUID().toString())
		schedule.order = order
		schedule.numSentences = numSentencesPerDay
		schedule.sentenceIndex = startingSentence - 1
		for (review in dailyReviews) {
			schedule.reviewPattern.add(review.toInt())
		}

		// build course
		val course: Course = realm.createObject(Course::class.java, UUID.randomUUID().toString())
		course.title = title

		course.languages.clear()
		course.languages.addAll(languages)
		course.pauseMillis = 1000
		course.schedule = schedule
		realm.commitTransaction()
		// --- end transaction
		return course
	}

	/**
	 * Toggles a pack in a course.
	 *
	 * If the pack is in the course it will be removed, otherwise it will be added.
	 *
	 * @param packId The ID of the pack to toggle.
	 * @param courseId The course ID of the pack which should be toggled.
	 */
	suspend fun togglePackInCourse(packId: String, courseId: String) {
		val r = Realm.getDefaultInstance()
		r.executeTransactionAwait {
			val course = it.where(Course::class.java).equalTo("id", courseId).findFirst()!!
			val pack = it.where(Pack::class.java).equalTo("id", packId).findFirst()!!
			if (course.packs.contains(pack))
				course.packs.remove(pack)
			else
				course.packs.add(pack)
		}
	}

	/**
	 * Fetches a single course.
	 *
	 * This fetches a single course and notifies [callback] whether it was successful or not.
	 *
	 * @param courseId The ID of the course to look for.
	 * @param callback If found, the course will be passed to this wrapped in [Result.Success],
	 * otherwise [Result.Failure] will be passed in.
	 */
	fun fetchCourse(courseId: String, callback: (result: Result<Course>) -> Unit) {
		val course = realm.where(Course::class.java).equalTo("id", courseId).findFirst()
		if (course == null)
			callback(Result.Failure(R.string.course_not_found))
		else
			callback(Result.Success(course))
	}
}