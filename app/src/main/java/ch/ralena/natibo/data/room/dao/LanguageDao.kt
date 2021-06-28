package ch.ralena.natibo.data.room.dao

import androidx.room.*
import ch.ralena.natibo.data.room.`object`.*

@Dao
interface LanguageDao {
	// region Get
	@Query("SELECT * FROM languageroom")
	suspend fun getAll(): List<LanguageRoom>

	@Transaction
	@Query("SELECT * FROM languageroom WHERE id = :id LIMIT 1")
	suspend fun getByIdWithPacks(id: Long): LanguageWithPacks?

	@Transaction
	@Query("SELECT * FROM languageroom")
	suspend fun getAllWithPacks(): List<LanguageWithPacks>

	@Query("SELECT * FROM languageroom WHERE id = :id LIMIT 1")
	suspend fun getById(id: Long): LanguageRoom?

	@Query("SELECT * FROM languageroom WHERE code = :code LIMIT 1")
	suspend fun getByCode(code: String): LanguageRoom?
	// endregion Get

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(language: LanguageRoom): Long

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertLanguagePackCrossRef(languagePackCrossRef: LanguagePackCrossRef)

	@Update
	suspend fun update(language: LanguageRoom)

	@Delete
	suspend fun delete(language: LanguageRoom)
}