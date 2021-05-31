package ch.ralena.natibo.data.room.dao

import androidx.room.*
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackRoom

@Dao
interface LanguageDao {
	@Query("SELECT * FROM languageroom")
	suspend fun getAll(): List<LanguageRoom>

	@Query ("SELECT * FROM languageroom WHERE id = :id LIMIT 1")
	suspend fun getById (id: String) : LanguageRoom

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(language: LanguageRoom): Long

	@Update
	suspend fun update(language: LanguageRoom)

	@Delete
	suspend fun delete(language: LanguageRoom)
}