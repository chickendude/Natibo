package ch.ralena.natibo.ui.language.importer.worker.usecase

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import ch.ralena.natibo.utils.Utils.readZip
import java.io.*
import java.util.zip.ZipInputStream
import javax.inject.Inject

private const val BUFFER_SIZE = 1024

class CountMp3sUseCase @Inject constructor(
	// used to calculate length of mp3 file
	private val metadataRetriever: MediaMetadataRetriever,
	private val context: Context
) {
	companion object {
		private val TAG = CountMp3sUseCase::class.java.simpleName
	}

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

	fun copyMp3s(inputStream: InputStream) {
		var bos: BufferedOutputStream
		readZip(inputStream) { zis ->
			var zipEntry = zis.nextEntry

			// loop through files in the archive
			var fileNumber = 0
			while (zipEntry != null) {
				val entryName = zipEntry.name
				if (entryName.contains(".mp3")) {
					val (language, packName, indexDotMp3) = entryName.split(" - ")

					// make sure directory has been created
					val folder = File("${context.filesDir}/$language/$packName")
					if (!folder.isDirectory) {
						folder.mkdirs()
					}

					// set up file path
					val audioFile = File("${folder.absolutePath}/$indexDotMp3")

					// actually write the file
					copyMp3ToInternalStorage(audioFile, zis)

					// now set up database objects which we will fill in after extracting all mp3s
					val index = indexDotMp3.replace(".mp3", "").toInt()
//					val lang = realm.where(
//						Language::class.java
//					).equalTo("languageId", language).findFirst()

					// calculate mp3s length
					val mp3Uri = audioFile.absolutePath
					metadataRetriever.setDataSource(mp3Uri)
					val mp3LengthMs =
						metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
							?.toInt()
//					val pack: Pack = lang!!.getPack(book)
//					pack.createSentenceOrUpdate(
//						realm,
//						index,
//						null,
//						null,
//						null,
//						mp3Uri,
//						mp3Length
//					)
				} else {
					Log.d(TAG, "Skipping: $entryName")
				}
//				progressSubject.onNext(++fileNumber)
				zipEntry = zis.nextEntry
			}
			inputStream.close()
			zis.close()
		}
	}

	// region Helper functions----------------------------------------------------------------------
	private fun copyMp3ToInternalStorage(audioFile: File, zis: ZipInputStream) {
		val buffer = ByteArray(BUFFER_SIZE)
		var numBytesRead: Int
		FileOutputStream(audioFile).use { fos ->
			BufferedOutputStream(fos, BUFFER_SIZE).use { bos ->
				while (zis.read(buffer, 0, BUFFER_SIZE)
						.also { numBytesRead = it } != -1
				) {
					bos.write(buffer, 0, numBytesRead)
				}
				bos.flush()
			}
		}
	}
	// endregion Helper functions-------------------------------------------------------------------
}
