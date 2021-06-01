package ch.ralena.natibo.ui.language.list

import android.view.View
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.ScreenNavigator
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LanguageListViewModel @Inject constructor(
	private val realm: Realm,
	private val screenNavigator: ScreenNavigator,
	private val languageRepository: LanguageRepository
) : BaseViewModel<LanguageListViewModel.Listener>() {
	interface Listener {
		fun onLanguagesLoaded(languages: List<LanguageRoom>)
	}

	lateinit var languages: List<LanguageRoom>

	fun fetchLanguages() {
		coroutineScope.launch(Dispatchers.Main) {
			withContext(Dispatchers.IO) {
				languages = languageRepository.fetchLanguages()
			}
			listeners.forEach { it.onLanguagesLoaded(languages) }
		}
	}

	fun languageSelected(language: LanguageRoom) {
		screenNavigator.toLanguageDetailsFragment(language.id)
	}

	fun getRecyclerViewVisibility(): Int =
		if (languages.isNullOrEmpty()) View.GONE else View.VISIBLE

	fun getNoCourseTextVisibility(): Int =
		if (languages.isNullOrEmpty()) View.VISIBLE else View.GONE

}