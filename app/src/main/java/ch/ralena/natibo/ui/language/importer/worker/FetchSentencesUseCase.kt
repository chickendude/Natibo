package ch.ralena.natibo.ui.language.importer.worker

import ch.ralena.natibo.utils.Utils.readZip
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

class FetchSentencesUseCase @Inject constructor() {
	suspend fun fetchSentences(inputStream: InputStream): List<String> {
		var sentences = emptyList<String>()

		readZip(inputStream) { zis ->
			var zipEntry = zis.nextEntry
			while (zipEntry != null) {
				if (zipEntry.name.endsWith(".gsp")) {
					sentences = readGspFile(zis)
				}
				zipEntry = zis.nextEntry
			}
		}
		return sentences
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