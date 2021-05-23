package ch.ralena.natibo.data.room.dao

import androidx.room.*
import ch.ralena.natibo.data.room.`object`.CourseRoom

@Dao
interface CourseDao {
	@Query("SELECT * FROM courseroom")
	fun getAll(): List<CourseRoom>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(course: CourseRoom)

	@Update
	suspend fun update(course: CourseRoom)

	@Delete
	suspend fun delete(course: CourseRoom)
}