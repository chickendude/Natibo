package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.room.`object`.Course
import io.realm.Realm
import javax.inject.Inject

class CourseRepository @Inject constructor(private val realm: Realm) {
	fun fetchCourses() = realm.where(Course::class.java).findAll()
}