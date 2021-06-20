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

	@Query("SELECT * FROM sentenceroom WHERE id = :id LIMIT 1")
	suspend fun getById(id: Long): SentenceRoom

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(sentence: SentenceRoom)

	@Update(onConflict = OnConflictStrategy.REPLACE)
	suspend fun update(sentence: SentenceRoom)

	@Query("UPDATE sentenceroom SET mp3 = :mp3Uri, mp3Length = :mp3Length WHERE packId = :packId AND `index` = :index")
	suspend fun updateMp3(packId: Long, index: Int, mp3Uri: String, mp3Length: Int)

	@Delete
	suspend fun delete(sentence: SentenceRoom)
}