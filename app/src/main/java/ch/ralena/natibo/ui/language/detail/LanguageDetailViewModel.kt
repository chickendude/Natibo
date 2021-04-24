package ch.ralena.natibo.ui.language.detail

import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.Pack
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.ScreenNavigator
import io.realm.Realm
import javax.inject.Inject

class LanguageDetailViewModel @Inject constructor(
		private val realm: Realm,
		private val screenNavigator: ScreenNavigator
): BaseViewModel<LanguageDetailViewModel.Listener>() {
	interface Listener {
		fun onLanguageLoaded(language: Language)
	}

	var language: Language? = null

	fun loadLanguage(id: String?) {
		language = realm.where(Language::class.java).equalTo("languageId", id).findFirst()
		language?.run {
			for (l in listeners)
				l.onLanguageLoaded(this)
		}
	}

	fun languagePackSelected(pack: Pack) {
		screenNavigator.toSentenceListFragment(pack.id, language!!.languageId)
	}
}