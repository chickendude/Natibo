package ch.ralena.natibo.data.room.dao

import androidx.room.*
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.PackRoom

@Dao
interface PackDao {
	@Query("SELECT * FROM packroom")
	suspend fun getAll(): List<PackRoom>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(pack: PackRoom)

	@Update
	suspend fun update(pack: PackRoom)

	@Delete
	suspend fun delete(pack: PackRoom)
}