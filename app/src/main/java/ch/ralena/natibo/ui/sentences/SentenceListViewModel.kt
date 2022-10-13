package ch.ralena.natibo.ui.sentences

import android.media.MediaPlayer
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.PackRepository
import ch.ralena.natibo.data.room.SentenceRepository
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class SentenceListViewModel @Inject constructor(
	private val languageRepository: LanguageRepository,
	private val packRepository: PackRepository,
	private val sentenceRepository: SentenceRepository,
	private val mediaPlayer: MediaPlayer,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<SentenceListViewModel.Listener>() {
	interface Listener {
		fun onInfoFetched(language: LanguageRoom, sentences: List<SentenceRoom>)
		fun onError()
	}

	private val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	fun fetchInfo(languageId: Long, packId: Long) {
		coroutineScope.launch {
			val language = languageRepository.fetchLanguage(languageId)
			val sentences = sentenceRepository.fetchSentencesInPack(languageId, packId)
			if (language == null) {
				listeners.forEach { it.onError() }
				return@launch
			}
			withContext(dispatcherProvider.main()) {
				listeners.forEach { it.onInfoFetched(language, sentences) }
			}
		}
	}

	fun playSentence(sentence: SentenceRoom) {
		try {
			mediaPlayer.reset()
			mediaPlayer.setDataSource(sentence.mp3)
			mediaPlayer.prepare()
			mediaPlayer.start()
		} catch (e: IOException) {
			e.printStackTrace()
			// todo pass error to listeners
//			Toast.makeText(
//				context,
//				String.format("Error loading audio for '%s'", sentence.original),
//				Toast.LENGTH_SHORT
//			).show()
		}
	}

}