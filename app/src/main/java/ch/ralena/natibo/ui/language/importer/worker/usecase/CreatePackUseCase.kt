package ch.ralena.natibo.ui.language.importer.worker.usecase

import android.util.Log
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.PackRepository
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

class CreatePackUseCase @Inject constructor(
	private val packRepository: PackRepository
) {
	suspend fun createPack(packName: String, languageCode: String): Long {
		var pack = packRepository.fetchPackByNameAndLanguage(packName, languageCode)
		// Create the pack if it doesn't exist already
		if (pack == null) {
			pack = PackRoom(packName, languageCode)
			return packRepository.createPack(pack)
		}
		return pack.id
	}
}