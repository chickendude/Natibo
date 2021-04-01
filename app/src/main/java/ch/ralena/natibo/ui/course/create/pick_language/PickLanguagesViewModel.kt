package ch.ralena.natibo.ui.course.create.pick_language

import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.di.module.SelectedLanguages
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.ScreenNavigator
import javax.inject.Inject

class PickLanguagesViewModel @Inject constructor(
		private val languageRepository: LanguageRepository,
		private val screenNavigator: ScreenNavigator,
		@SelectedLanguages private val selectedLanguages: ArrayList<Language>
) : BaseViewModel<PickLanguagesViewModel.Listener>() {
	interface Listener {
		fun onLanguagesLoaded(languages: List<Language>)
		fun onUpdateCheckMenuVisibility(isVisible: Boolean)
		fun onLanguageAdded(language: Language)
		fun onLanguageRemoved(language: Language)
	}

	fun fetchLanguages() {
		val languages = languageRepository.fetchLanguagesSorted()
		for (l in listeners)
			l.onLanguagesLoaded(languages)
	}

	fun addRemoveLanguage(language: Language) {
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
		val languageIds = selectedLanguages.map { it.languageId }
		screenNavigator.toCoursePreparationFragment(languageIds)
	}
}