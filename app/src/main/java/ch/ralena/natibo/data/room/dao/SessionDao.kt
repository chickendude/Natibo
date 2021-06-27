package ch.ralena.natibo.data.room.dao

import androidx.room.*
import ch.ralena.natibo.data.room.`object`.SessionRoom
import ch.ralena.natibo.data.room.`object`.SessionSentenceCrossRef
import ch.ralena.natibo.data.room.`object`.SessionWithSentences

@Dao
interface SessionDao {
	@Query("SELECT * FROM sessionroom")
	suspend fun getAll(): List<SessionRoom>

	@Query("SELECT * FROM sessionroom WHERE id = :id")
	suspend fun getById(id: Long): SessionRoom?

	@Query("SELECT * FROM sessionroom WHERE id = :id")
	suspend fun getSessionWithSentencesById(id: Long): SessionWithSentences

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSessionSentenceCrossRef(sessionSentenceCrossRef: SessionSentenceCrossRef)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(course: SessionRoom): Long

	@Update
	suspend fun update(course: SessionRoom)

	@Delete
	suspend fun delete(course: SessionRoom)
}