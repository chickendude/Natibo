package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.data.room.dao.SentenceDao
import javax.inject.Inject

class SentenceRepository @Inject constructor(
	private val sentenceDao: SentenceDao
) {
	suspend fun createSentence(sentence: SentenceRoom) = sentenceDao.insert(sentence)

	suspend fun fetchSentence(id: Long): SentenceRoom = sentenceDao.getById(id)

	suspend fun fetchSentences(languageId: Long): List<SentenceRoom> =
		sentenceDao.getAllInLanguage(languageId)

	suspend fun fetchSentenceCount(languageId: Long): Int =
		sentenceDao.getCountInLanguage(languageId)

	/**
	 * Fetches all sentences available in the pack.
	 *
	 * If [start] and [end] are provided, the sentences will be filtered. Both values must be
	 * present and [start] must be less than [end].
	 *
	 * @param packId Id of the [PackRoom] whose sentences should be grabbed.
	 * @param start The starting index (inclusive), the first sentence in the pack to grab.
	 * @param end The ending index (non-inclusive), the sentence in the pack to grab to stop at.
	 */
	suspend fun fetchSentencesInPack(
		packId: Long,
		languageId: Long,
		start: Int = 0,
		end: Int = 0
	): List<SentenceRoom> =
		if (start > 0 && end > 0 && start < end)
			sentenceDao.getPackSentencesInRange(packId, languageId, start, end)
		else
			sentenceDao.getAllInPack(packId, languageId)

	suspend fun updateSentence(sentence: SentenceRoom) =
		sentenceDao.update(sentence)

	suspend fun updateSentenceMp3(packId: Long, index: Int, mp3Uri: String, mp3Length: Int) =
		sentenceDao.updateMp3(packId, index, mp3Uri, mp3Length)
}