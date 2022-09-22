package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.data.room.`object`.SessionRoom
import ch.ralena.natibo.data.room.`object`.SessionSentenceCrossRef
import ch.ralena.natibo.data.room.`object`.SessionWithSentences
import ch.ralena.natibo.data.room.dao.SessionDao
import ch.ralena.natibo.model.NatiboSession
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

	suspend fun saveNatiboSession(natiboSession: NatiboSession) {
		val session = fetchSession(natiboSession.sessionId)
		session?.copy(progress = natiboSession.currentSentenceIndex)?.let { updatedSession ->
			sessionDao.update(updatedSession)
		}
	}

	// region Delete -------------------------------------------------------------------------------
	suspend fun deleteSession(session: SessionRoom) {
		sessionDao.deleteSessionSentenceCrossRefs(session.id)
		sessionDao.delete(session)
	}

	suspend fun deleteSession(id: Long) {
		sessionDao.deleteSessionSentenceCrossRefs(id)
		sessionDao.delete(id)
	}

	suspend fun deleteAll() {
		// TODO: Delete sentence crossrefs
		sessionDao.deleteAll()
	}
	// endregion Delete ----------------------------------------------------------------------------
}