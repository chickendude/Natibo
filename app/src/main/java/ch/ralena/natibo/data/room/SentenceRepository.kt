package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.data.room.dao.SentenceDao
import javax.inject.Inject

class SentenceRepository @Inject constructor(
	private val sentenceDao: SentenceDao
) {
	suspend fun createSentence(sentence: SentenceRoom) = sentenceDao.insert(sentence)

	suspend fun fetchSentence(id: Long): SentenceRoom = sentenceDao.getById(id)

	suspend fun fetchSentences(): List<SentenceRoom> =
		sentenceDao.getAll()

	suspend fun fetchSentencesInPack(packId: Long): List<SentenceRoom> =
		sentenceDao.getAllInPack(packId)

	suspend fun updateSentence(sentence: SentenceRoom) =
		sentenceDao.update(sentence)

	suspend fun updateSentenceMp3(packId: Long, index: Int, mp3Uri: String, mp3Length: Int) =
		sentenceDao.updateMp3(packId, index, mp3Uri, mp3Length)
}