package ch.ralena.natibo.ui.language.importer.worker

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.ui.language.importer.worker.usecase.*
import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.Exception
import javax.inject.Inject

class ImportException(message: String) : Exception(message)

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
) : BaseViewModel<PackImporterViewModel.Listener>() {
	interface Listener {
		fun onNotificationUpdate(message: String)
		fun onProgressUpdate(progress: Int)
		fun onError(exception: ImportException)
		fun onWarning(warningMsg: String)
		fun onImportComplete()
	}

	private val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	private var numMp3s = 0

	/**
	 * Pulls the language code and pack name out from the Uri filename.
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
			// Grab language and pack name
			updateNotification("Reading file")
			val (languageCode, packName) = readPackDataUseCase.extractLanguageAndPackName(uri)
			updateProgress(1)
			val languageId = createLanguageUseCase.fetchOrCreateLanguage(languageCode)
			updateProgress(2)
			val packId = createPackUseCase.createPack(packName, languageId)
			updateProgress(3)
			numMp3s = countMp3sUseCase.countMp3Files(getInputStream(uri))
			updateProgress(4)
			val sentences = fetchSentencesUseCase.fetchSentences(getInputStream(uri))
			updateProgress(6)
			checkSentences(numMp3s, sentences)
			createSentencesUseCase.createSentences(languageId, packId, sentences)
			updateProgress(20)
			// copy mp3 files over
			updateNotification("Extracting mp3s")
			countMp3sUseCase.copyMp3s(packId, getInputStream(uri))
			updateProgress(100)
			updateNotification("Mp3s copied over")
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