package ch.ralena.natibo.ui.language.importer.worker.usecase

import android.util.Log
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.SentenceRepository
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.ui.language.importer.worker.ImportException
import ch.ralena.natibo.ui.language.importer.worker.PackImporterWorker
import ch.ralena.natibo.utils.Utils.readZip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

class CreateLanguageUseCase @Inject constructor(
	private val languageRepository: LanguageRepository
) {
	suspend fun createLanguage(languageCode: String): Long {
		// TODO: Make sure language isn't duplicated when creating it
		val languageId = languageRepository.createLanguage(languageCode)
			?: throw ImportException("Unable to create language with id: $languageCode")
		return languageId
	}
}