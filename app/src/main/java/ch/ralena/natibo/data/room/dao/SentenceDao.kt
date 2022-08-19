package ch.ralena.natibo.data.room.dao

import androidx.room.*
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom

@Dao
interface SentenceDao {
	@Query("SELECT * FROM sentenceroom WHERE languageId = :languageId")
	suspend fun getAllInLanguage(languageId: Long): List<SentenceRoom>

	@Query("SELECT COUNT(ID) FROM sentenceroom WHERE languageId = :languageId")
	suspend fun getCountInLanguage(languageId: Long): Int

	@Query("SELECT * FROM sentenceroom WHERE packId = :packId AND languageId = :languageId")
	suspend fun getAllInPack(packId: Long, languageId: Long): List<SentenceRoom>

	@Query("SELECT * FROM sentenceroom WHERE packId = :packId AND languageId = :languageId AND `index` >= :start AND `index` < :end")
	suspend fun getPackSentencesInRange(
		packId: Long,
		languageId: Long,
		start: Int,
		end: Int
	): List<SentenceRoom>

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