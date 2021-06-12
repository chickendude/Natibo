package ch.ralena.natibo.ui.course.create.pick_language

import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.di.module.LanguageList
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.ScreenNavigator
import kotlinx.coroutines.*
import javax.inject.Inject

class PickLanguagesViewModel @Inject constructor(
		private val languageRepository: LanguageRepository,
		private val screenNavigator: ScreenNavigator,
		@LanguageList private val selectedLanguages: ArrayList<LanguageRoom>,
		private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<PickLanguagesViewModel.Listener>() {
	interface Listener {
		fun onLanguagesLoaded(languages: List<LanguageRoom>)
		fun onUpdateCheckMenuVisibility(isVisible: Boolean)
		fun onLanguageAdded(language: LanguageRoom)
		fun onLanguageRemoved(language: LanguageRoom)
	}
	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	fun fetchLanguages() {
		coroutineScope.launch(dispatcherProvider.main()) {
			var languages: List<LanguageRoom>
			withContext(dispatcherProvider.io()) {
				languages = languageRepository.fetchLanguages()
			}
			listeners.forEach { it.onLanguagesLoaded(languages) }
		}
	}

	fun addRemoveLanguage(language: LanguageRoom) {
		if (selectedLanguages.contains(language)) {
			selectedLanguages.remove(language)
			for (l in listeners)
				l.onLanguageRemoved(language)
		} else {
			selectedLanguages.add(language)
			for (l in listeners)
				l.onLanguageAdded(language)
		}
		updateCheckMenuVisibility()
	}

	fun updateCheckMenuVisibility() {
		if (selectedLanguages.size <= 1)
			for (l in listeners)
				l.onUpdateCheckMenuVisibility(selectedLanguages.size > 0)
	}

	fun languagesConfirmed() {
		val languageIds = selectedLanguages.map { it.id }
		screenNavigator.toCoursePreparationFragment(languageIds)
	}
}