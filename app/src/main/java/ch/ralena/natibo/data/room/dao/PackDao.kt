package ch.ralena.natibo.data.room.dao

import androidx.room.*
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.data.room.`object`.PackWithSentences

@Dao
interface PackDao {
	@Query("SELECT * FROM packroom")
	suspend fun getAll(): List<PackRoom>

	@Query("SELECT * FROM packroom WHERE id = :id LIMIT 1")
	suspend fun getById(id: Long): PackRoom?

	@Query("SELECT * FROM packroom WHERE name = :name LIMIT 1")
	suspend fun getByName(name: String): PackRoom?

	@Transaction
	@Query("SELECT * FROM packroom WHERE id = :id LIMIT 1")
	suspend fun getWithSentencesById(id: Long): PackWithSentences?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(pack: PackRoom): Long

	@Update
	suspend fun update(pack: PackRoom)

	@Delete
	suspend fun delete(pack: PackRoom)
}