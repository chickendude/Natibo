package ch.ralena.natibo.ui.language.importer.worker

import ch.ralena.natibo.ui.base.BaseListener
import ch.ralena.natibo.utils.Utils.readZip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
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
