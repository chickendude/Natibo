package ch.ralena.natibo.ui.language.importer.worker

import ch.ralena.natibo.ui.base.BaseListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal class CountFilesUseCase {
	interface Listener {
		suspend fun onUpdateProgress(progress: Int)
		suspend fun onSentencesLoaded(sentences: List<String>)
	}

	suspend fun countFiles(zis: ZipInputStream, listener: Listener) {
		// update action in fragment
		var zipEntry: ZipEntry
		var numFiles = 0

		// calculate number of files
		var baseLanguage = ""
		var targetLanguage = ""
		var packName = ""
		var hasGspFile = false
		while (zis.nextEntry.also { zipEntry = it } != null) {
			if (zipEntry.name.endsWith("mp3")) {
				numFiles++
				listener.onUpdateProgress(numFiles)
				if (packName.isEmpty())
					packName = zipEntry.name.split(" - ").toTypedArray()[1]
			} else if (zipEntry.name.endsWith(".gsp")) {
				hasGspFile = true
				// extract base language and target language from file name
				val nameParts = zipEntry.name.split("-").toTypedArray()
				baseLanguage = nameParts[0].trim { it <= ' ' }
				if (nameParts.size > 3)
					targetLanguage = nameParts[1].trim { it <= ' ' }
				// extract contents of file into the StringBuilder
				val sentences = withContext(Dispatchers.IO) {
					val sentences = readGspFile(zis)
					listener.onSentencesLoaded(sentences)
					sentences
				}
				listener.onSentencesLoaded(sentences)
			}
		}
	}

	// region Helper functions----------------------------------------------------------------------
	/**
	 * Reads the .gsp file and returns the list of sentences from it.
	 *
	 * @param zis ZipInputStream positioned at the .gsp file.
	 * @return List of strings containing the lines from the .gsp file.
	 */
	private fun readGspFile(zis: ZipInputStream): List<String> {
		val buffer = ByteArray(PackImporterWorker.BUFFER_SIZE)
		var numBytesRead: Int
		val baos = ByteArrayOutputStream()
		while (
			zis.read(
				buffer,
				0,
				PackImporterWorker.BUFFER_SIZE
			).also { numBytesRead = it } >= 0
		) {
			baos.write(buffer, 0, numBytesRead)
		}
		val text = baos.toString("UTF-8").trim('\n')
		return text.split("\n")
	}
	// endregion Helper functions-------------------------------------------------------------------
}