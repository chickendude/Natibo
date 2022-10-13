package ch.ralena.natibo.data.room.dao

import androidx.room.*
import ch.ralena.natibo.data.room.`object`.CourseRoom

@Dao
interface CourseDao {
	@Query("SELECT * FROM courseroom")
	suspend fun getAll(): List<CourseRoom>

	@Query("SELECT * FROM courseroom WHERE id = :id")
	suspend fun getCourseById(id: Long): CourseRoom?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(course: CourseRoom): Long

	@Update
	suspend fun update(course: CourseRoom)

	@Delete
	suspend fun delete(course: CourseRoom)

	@Query("SELECT COUNT(id) FROM sessionroom WHERE courseId = :courseId")
	suspend fun getSessionCount(courseId: Long): Int
}