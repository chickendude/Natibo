package ch.ralena.natibo.ui.course.create

import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.ui.base.BaseViewModel
import javax.inject.Inject

class CoursePickLanguageViewModel @Inject constructor(
		private val languageRepository: LanguageRepository
) : BaseViewModel<CoursePickLanguageViewModel.Listener>() {
	interface Listener {
		fun languagesLoaded(languages: List<Language>)
	}

	fun fetchLanguages() {
		val languages = languageRepository.fetchLanguagesSorted()
		for (l in listeners)
			l.languagesLoaded(languages)
	}
}