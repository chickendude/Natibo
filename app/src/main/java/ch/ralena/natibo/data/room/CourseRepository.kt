package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.Schedule
import io.realm.Realm
import io.realm.RealmResults
import java.util.*
import javax.inject.Inject

class CourseRepository @Inject constructor(private val realm: Realm) {
	fun fetchCourses(): RealmResults<Course> = realm.where(Course::class.java).findAll()

	fun createCourse(order: String, numSentencesPerDay: Int, dailyReviews: Array<String>, title: String, languages: List<Language>): Course {
		// --- begin transaction
		realm.beginTransaction()
		// create sentence schedule
		val schedule: Schedule = realm.createObject<Schedule>(Schedule::class.java, UUID.randomUUID().toString())
		schedule.setOrder(order.toString())
		schedule.setNumSentences(numSentencesPerDay)
		for (review in dailyReviews) {
			schedule.getReviewPattern().add(review.toInt())
		}

		// build course
		val course: Course = realm.createObject<Course>(Course::class.java, UUID.randomUUID().toString())
		course.setTitle(title)

		course.languages.clear()
		course.languages.addAll(languages)
		course.setPauseMillis(1000)
		course.setSchedule(schedule)
		realm.commitTransaction()
		// --- end transaction
		return course
	}
}