package ch.ralena.natibo.ui.course.create

import android.os.Bundle
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.di.module.SelectedLanguages
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.ui.fragment.CoursePreparationFragment
import ch.ralena.natibo.utils.ScreenNavigator
import javax.inject.Inject

class CoursePickLanguageViewModel @Inject constructor(
		private val languageRepository: LanguageRepository,
		private val screenNavigator: ScreenNavigator,
		@SelectedLanguages private val selectedLanguages: ArrayList<Language>
) : BaseViewModel<CoursePickLanguageViewModel.Listener>() {
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
		if (selectedLanguages.size <= 1)
			for (l in listeners)
				l.onUpdateCheckMenuVisibility(selectedLanguages.size > 0)
	}

	fun languagesConfirmed() {
		val languageIds = selectedLanguages.map { it.languageId }
		screenNavigator.toCoursePreparationFragment(languageIds)
	}
}