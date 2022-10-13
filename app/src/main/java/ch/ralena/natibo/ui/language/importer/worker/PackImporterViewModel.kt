package ch.ralena.natibo.ui.language.importer.worker

import android.content.ContentResolver
import android.net.Uri
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.ui.language.importer.worker.listener.PackImporterListener
import ch.ralena.natibo.ui.language.importer.worker.usecase.*
import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject

class ImportException(message: String) : Exception(message)

/**
 * ViewModel for [PackImporterWorker].
 *
 * Its main job is to handle the actual importing process and to send progress reports back to the
 * main worker.
 */
class PackImporterViewModel @Inject constructor(
	private val contentResolver: ContentResolver,
	private val dispatcherProvider: DispatcherProvider,
	// Use cases
	private val countMp3sUseCase: CountMp3sUseCase,
	private val createLanguageUseCase: CreateLanguageUseCase,
	private val createPackUseCase: CreatePackUseCase,
	private val createSentencesUseCase: CreateSentencesUseCase,
	private val fetchSentencesUseCase: FetchSentencesUseCase,
	private val readPackDataUseCase: ReadPackDataUseCase
) : BaseViewModel<PackImporterListener>() {
	private val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	private var numMp3s = 0

	/**
	 * Pulls the language code and pack name out from the Uri filename.
	 *
	 * @param uri The Uri pointing to the file to be imported.
	 */
	suspend fun importPack(uri: Uri) {
		coroutineScope.launch {
			createSentencesUseCase.sentenceCount()
				.collect { updateNotification("Creating sentences: $it") }
		}
		coroutineScope.launch {
			countMp3sUseCase.mp3Count()
				.collect {
					updateNotification("Copying mp3: $it / $numMp3s")
					updateProgress((it * (100 - 25)) / numMp3s + 25)
				}
		}

		try {
			u("Reading file")

			u("Reading file data")
			val (languageCode, packName) = readPackDataUseCase.extractLanguageAndPackName(uri)
			updateProgress(1)

			u("Creating language")
			val languageId = createLanguageUseCase.fetchOrCreateLanguage(languageCode)
			updateProgress(2)

			u("Creating pack")
			val packId = createPackUseCase.createPack(packName, languageId)
			updateProgress(3)

			u("Counting mp3s")
			numMp3s = countMp3sUseCase.countMp3Files(getInputStream(uri))
			updateProgress(6)

			u("Extracting sentences")
			val sentences = fetchSentencesUseCase.fetchSentences(getInputStream(uri))
			updateProgress(8)

			u("Creating sentences")
			checkSentences(numMp3s, sentences)
			createSentencesUseCase.createSentences(languageId, packId, sentences)
			updateProgress(20)

			u("Extracting mp3s")
			countMp3sUseCase.copyMp3s(packId, languageId, getInputStream(uri))
			u("Copy complete")
			updateProgress(100)
		} catch (e: ImportException) {
			listeners.forEach { it.onError(e) }
		}
		listeners.forEach { it.onImportComplete() }
	}

	// region Helper functions----------------------------------------------------------------------
	/**
	 * Perform some checks to notify user of any abnormal issues such as missing sentence texts,
	 * audio files, mismatching numbers, etc.
	 */
	private suspend fun checkSentences(numMp3s: Int, sentences: List<String>) {
		if (sentences.isEmpty()) {
			sendWarning("Sentence list is empty.")
		} else if (numMp3s == 0) {
			sendWarning("No audio files were found.")
		} else if (numMp3s != sentences.size - 1) {
			sendWarning("There are $numMp3s mp3s but ${sentences.size} sentences.")
		}
	}

	/**
	 * Updates the notification and action texts.
	 */
	private fun u(message: String) {
		listeners.forEach {
			it.onNotificationUpdate(message)
			it.onActionTextUpdate(message)
		}
	}

	private fun updateNotification(message: String) {
		listeners.forEach { it.onNotificationUpdate(message) }
	}

	private fun updateProgress(progress: Int) {
		listeners.forEach { it.onProgressUpdate(progress) }
	}

	private fun getInputStream(uri: Uri): InputStream {
		return when (uri.scheme) {
			"file" -> FileInputStream(File(uri.path!!))
			else -> contentResolver.openInputStream(uri)!!
		}
	}

	private suspend fun sendWarning(msg: String) {
		withContext(dispatcherProvider.main()) {
			listeners.forEach { it.onWarning(msg) }
		}
	}
	// endregion Helper functions-------------------------------------------------------------------
}