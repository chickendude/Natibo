package ch.ralena.natibo.data.room.dao

import androidx.room.*
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom

@Dao
interface SentenceDao {
	@Query("SELECT * FROM sentenceroom")
	suspend fun getAll(): List<SentenceRoom>

	@Query("SELECT * FROM sentenceroom WHERE packId = :packId")
	suspend fun getAllInPack(packId: Long): List<SentenceRoom>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(sentence: SentenceRoom)

	@Update(onConflict = OnConflictStrategy.REPLACE)
	suspend fun update(sentence: SentenceRoom)

	@Delete
	suspend fun delete(sentence: SentenceRoom)
}