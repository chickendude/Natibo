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
	suspend fun insert(session: SessionRoom): Long

	@Update
	suspend fun update(session: SessionRoom)

	@Query("DELETE FROM sessionroom WHERE id > 0")
	suspend fun deleteAll()

	@Query("DELETE FROM sessionroom WHERE id = :id")
	suspend fun delete(id: Long)

	@Delete
	suspend fun delete(session: SessionRoom)

	@Query("DELETE FROM sessionsentencecrossref WHERE sessionId = :id")
	suspend fun deleteSessionSentenceCrossRefs(id: Long)
}