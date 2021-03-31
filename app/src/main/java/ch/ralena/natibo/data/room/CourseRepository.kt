package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.room.`object`.Course
import io.realm.Realm
import io.realm.RealmResults
import javax.inject.Inject

class CourseRepository @Inject constructor(private val realm: Realm) {
	fun fetchCourses(): RealmResults<Course> = realm.where(Course::class.java).findAll()
}