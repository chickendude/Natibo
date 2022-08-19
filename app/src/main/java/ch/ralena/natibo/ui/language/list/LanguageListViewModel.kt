package ch.ralena.natibo.ui.language.list

import android.view.View
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.SentenceRepository
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.LanguageWithPacks
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.ScreenNavigator
import io.realm.Realm
import kotlinx.coroutines.*
import javax.inject.Inject

class LanguageListViewModel @Inject constructor(
	private val screenNavigator: ScreenNavigator,
	private val languageRepository: LanguageRepository,
	private val sentenceRepository: SentenceRepository,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<LanguageListViewModel.Listener>() {
	interface Listener {
		fun onLanguagesLoaded(languagesWithPacks: List<LanguageWithPacks>, sentenceCounts: List<Int>)
	}

	private val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	private var languagesWithPacks: List<LanguageWithPacks> = mutableListOf()

	fun fetchLanguages() {
		coroutineScope.launch(dispatcherProvider.main()) {
			val sentenceCounts = mutableListOf<Int>()
			withContext(dispatcherProvider.io()) {
				languagesWithPacks = languageRepository.fetchLanguagesWithPacks()
				languagesWithPacks.forEach {
					val count = sentenceRepository.fetchSentenceCount(it.language.id)
					sentenceCounts.add(count)
				}
			}
			listeners.forEach { it.onLanguagesLoaded(languagesWithPacks, sentenceCounts) }
		}
	}

	fun languageSelected(language: LanguageRoom) {
		screenNavigator.toLanguageDetailsFragment(language.id)
	}

	fun getRecyclerViewVisibility(): Int =
		if (languagesWithPacks.isEmpty()) View.GONE else View.VISIBLE

	fun getNoCourseTextVisibility(): Int =
		if (languagesWithPacks.isEmpty()) View.VISIBLE else View.GONE
}