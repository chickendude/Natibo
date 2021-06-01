package ch.ralena.natibo.ui.language.importer.worker.usecase

import ch.ralena.natibo.data.room.SentenceRepository
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.ui.language.importer.worker.PackImporterWorker
import ch.ralena.natibo.utils.Utils.readZip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

class CreateSentencesUseCase @Inject constructor(
	private val sentenceRepository: SentenceRepository
) {
	private var sentenceCount: Int = 0

	fun sentenceCount(): Flow<Int> = flow {
		emit(sentenceCount)
		kotlinx.coroutines.delay(100)
	}

	suspend fun createSentences(languageId: Long, packId: Long, sentences: List<String>) {
		val packSentences = sentenceRepository.fetchSentencesInPack(packId)

		val sections = sentences[0].split("\t")

		sentences.drop(1).forEach {
			createSentence(packId, it.split('\t'), sections, packSentences)
			sentenceCount++
		}
	}

	// region Helper functions----------------------------------------------------------------------
	private suspend fun createSentence(
		packId: Long,
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
				SentenceRoom(index, original, alternate, romanization, ipa, "", 0, packId)
			)
		}
	}
	// endregion Helper functions-------------------------------------------------------------------
}