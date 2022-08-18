package ch.ralena.natibo.ui.language.importer.worker.usecase

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import ch.ralena.natibo.data.room.SentenceRepository
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.Utils.readZip
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.*
import java.util.zip.ZipInputStream
import javax.inject.Inject

private const val BUFFER_SIZE = 1024

class CountMp3sUseCase @Inject constructor(
	// used to calculate length of mp3 file
	private val metadataRetriever: MediaMetadataRetriever,
	private val sentenceRepository: SentenceRepository,
	dispatcherProvider: DispatcherProvider,
	@ApplicationContext private val context: Context
) {
	companion object {
		private val TAG = CountMp3sUseCase::class.java.simpleName
	}

	private val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	private var currentFile: Int = 0
	private var isProcessing = true

	fun mp3Count(): Flow<Int> = flow {
		while (isProcessing) {
			if (currentFile > 0) {
				emit(currentFile)
			}
			delay(200)
		}
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

	fun copyMp3s(packId: Long, inputStream: InputStream) {
		readZip(inputStream) { zis ->
			var zipEntry = zis.nextEntry

			// loop through files in the archive
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

					// calculate mp3s length
					val mp3Uri = audioFile.absolutePath
					metadataRetriever.setDataSource(mp3Uri)
					val mp3LengthMs =
						metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
							?.toInt() ?: 0

					coroutineScope.launch {
						sentenceRepository.updateSentenceMp3(packId, index, mp3Uri, mp3LengthMs)
					}
					currentFile++
				} else {
					Log.d(TAG, "Skipping: $entryName")
				}
				zipEntry = zis.nextEntry
			}
			inputStream.close()
			zis.close()
		}
		isProcessing = false
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
