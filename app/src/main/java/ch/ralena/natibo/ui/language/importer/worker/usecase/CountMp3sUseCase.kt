package ch.ralena.natibo.ui.language.importer.worker.usecase

import ch.ralena.natibo.utils.Utils.readZip
import java.io.InputStream
import javax.inject.Inject

class CountMp3sUseCase @Inject constructor() {
	fun countMp3Files(inputStream: InputStream): Int {
		var numFiles = 0
		readZip(inputStream) { zis ->
			var zipEntry = zis.nextEntry
			while (zipEntry != null) {
				if (zipEntry.name.endsWith("mp3"))
					numFiles++
				zipEntry = zis.nextEntry
			}
		}
		return numFiles
	}
}
