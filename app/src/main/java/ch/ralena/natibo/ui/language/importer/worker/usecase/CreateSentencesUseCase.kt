package ch.ralena.natibo.ui.language.importer.worker.usecase

import ch.ralena.natibo.data.room.SentenceRepository
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CreateSentencesUseCase @Inject constructor(
	private val sentenceRepository: SentenceRepository
) {
	private var sentenceCount: Int = 0
	private var totalSentences: Int = 1

	fun sentenceCount(): Flow<Int> = flow {
		while (sentenceCount < totalSentences) {
			if (sentenceCount > 0) {
				emit(sentenceCount)
			}
			delay(500)
		}
	}

	suspend fun createSentences(languageId: Long, packId: Long, sentences: List<String>) {
		totalSentences = sentences.size - 1
		val packSentences = sentenceRepository.fetchSentencesInPack(languageId, packId)

		val sections = sentences[0].split("\t")

		// Go through all sentences except first row which is the header information
		sentences.drop(1).forEach {
			createSentence(packId, languageId, it.split('\t'), sections, packSentences)
			sentenceCount++
		}
	}

	// region Helper functions----------------------------------------------------------------------
	private suspend fun createSentence(
		packId: Long,
		languageId: Long,
		parts: List<String>,
		sections: List<String>,
		packSentences: List<SentenceRoom>
	) {
		// Extract parts
		val index = parts[0].toInt()
		var original = ""
		var alternate = ""
		var ipa = ""
		var romanization = ""
		for (j in parts.indices) {
			val value = parts[j]
			when (sections[j].lowercase()) {
				"sentence" -> original = value
				"alternate" -> alternate = value
				"ipa" -> ipa = value
				"romanization" -> romanization = value
				else -> Unit
			}
		}
		// Create/update sentence
		val sentence = packSentences.firstOrNull { it.index == index }
		if (sentence != null) {
			sentenceRepository.updateSentence(
				sentence.copy(
					index = index,
					original = original,
					alternate = alternate,
					romanization = romanization,
					ipa = ipa
				)
			)
		} else {
			sentenceRepository.createSentence(
				SentenceRoom(
					index,
					original,
					alternate,
					romanization,
					ipa,
					"",
					0,
					packId,
					languageId
				)
			)
		}
	}
	// endregion Helper functions-------------------------------------------------------------------
}