package ch.ralena.natibo.data.room.dao

import androidx.room.*
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.PackRoom

@Dao
interface PackDao {
	@Query("SELECT * FROM packroom")
	suspend fun getAll(): List<PackRoom>

	@Query("SELECT * FROM packroom WHERE name = :name AND languageCode = :languageCode LIMIT 1")
	suspend fun getByNameAndLanguage(name: String, languageCode: String): PackRoom?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(pack: PackRoom): Long

	@Update
	suspend fun update(pack: PackRoom)

	@Delete
	suspend fun delete(pack: PackRoom)
}