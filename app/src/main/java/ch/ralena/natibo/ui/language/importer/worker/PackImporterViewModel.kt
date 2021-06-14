package ch.ralena.natibo.ui.language.importer.worker

import android.content.ContentResolver
import android.net.Uri
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.ui.language.importer.worker.usecase.*
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
		fun onError(exception: ImportException)
		fun onWarning(warningMsg: String)
	}

	/**
	 * Pulls the language code and pack name out from the Uri filename.
	 */
	suspend fun importPack(uri: Uri) {
		try {
			// Grab language and pack name
			val (languageCode, packName) = readPackDataUseCase.extractLanguageAndPackName(uri)
			val languageId = createLanguageUseCase.fetchOrCreateLanguage(languageCode)
			val packId = createPackUseCase.createPack(packName, languageCode)
			val numMp3s = countMp3sUseCase.countMp3Files(getInputStream(uri))
			val sentences = fetchSentencesUseCase.fetchSentences(getInputStream(uri))
			checkSentences(numMp3s, sentences)
			createSentencesUseCase.createSentences(languageId, packId, sentences)
			createSentencesUseCase.sentenceCount()
				.onEach { updateNotification("Reading sentence: $it") }.collect()
			// copy mp3 files over
			countMp3sUseCase.copyMp3s(getInputStream(uri))
			updateNotification("Mp3s copied over")
		} catch (e: ImportException) {
			listeners.forEach { it.onError(e) }
		}
	}

	// region Helper functions----------------------------------------------------------------------
	/**
	 * Perform some checks to notify user of any abnormal issues such as missing sentence texts,
	 * audio files, mismatching numbers, etc.
	 */
	private fun checkSentences(numMp3s: Int, sentences: List<String>) {
		if (sentences.isEmpty()) {
			sendWarning("Sentence list is empty.")
		} else if (numMp3s == 0) {
			sendWarning("No audio files were found.")
		} else if (numMp3s != sentences.size) {
			sendWarning("There are $numMp3s mp3s but ${sentences.size} sentences.")
		} else {
			// Todo: Probably don't need this in the end.
			updateNotification(sentences.last())
		}
	}

	private fun updateNotification(message: String) {
		listeners.forEach { it.onNotificationUpdate(message) }
	}

	private fun getInputStream(uri: Uri): InputStream {
		return when (uri.scheme) {
			"file" -> FileInputStream(File(uri.path!!))
			else -> contentResolver.openInputStream(uri)!!
		}
	}

	private fun sendWarning(msg: String) {
		for (l in listeners) l.onWarning(msg)
	}
// endregion Helper functions-------------------------------------------------------------------
}