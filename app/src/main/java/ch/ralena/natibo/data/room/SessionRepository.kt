package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.data.room.`object`.SessionRoom
import ch.ralena.natibo.data.room.`object`.SessionSentenceCrossRef
import ch.ralena.natibo.data.room.`object`.SessionWithSentences
import ch.ralena.natibo.data.room.dao.SessionDao
import javax.inject.Inject

class SessionRepository @Inject constructor(
	private val sessionDao: SessionDao
) {
	suspend fun createSession(session: SessionRoom) = sessionDao.insert(session)

	suspend fun fetchSession(id: Long): SessionRoom? = sessionDao.getById(id)

	suspend fun fetchSessionWithSentencesById(id: Long): SessionWithSentences =
		sessionDao.getSessionWithSentencesById(id)

	suspend fun addSentencesToSession(sessionId: Long, sentences: List<SentenceRoom>) {
		sentences.forEach {
			val sessionSentenceCrossRef = SessionSentenceCrossRef(sessionId, it.id)
			sessionDao.insertSessionSentenceCrossRef(sessionSentenceCrossRef)
		}
	}
}