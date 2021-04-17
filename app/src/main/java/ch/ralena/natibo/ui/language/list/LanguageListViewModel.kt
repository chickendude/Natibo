package ch.ralena.natibo.ui.language.list

import android.view.View
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.ScreenNavigator
import io.realm.Realm
import javax.inject.Inject

class LanguageListViewModel @Inject constructor(
		private val realm: Realm,
		private val screenNavigator: ScreenNavigator
) : BaseViewModel<LanguageListViewModel.Listener>() {
	interface Listener {
		fun onLanguagesLoaded(languages: List<Language>)
	}

	lateinit var languages: List<Language>

	fun fetchLanguages() {
		languages = Language.getLanguagesSorted(realm)
		for (l in listeners)
			l.onLanguagesLoaded(languages)
	}

	fun languageSelected(language: Language) {
		screenNavigator.toLanguageDetailsFragment(language.languageId)
	}

	fun getRecyclerViewVisibility(): Int =
			if (languages.isEmpty()) View.GONE else View.VISIBLE

	fun getNoCourseTextVisibility(): Int =
			if (languages.isEmpty()) View.VISIBLE else View.GONE

}