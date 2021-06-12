package ch.ralena.natibo.ui.language.list

import android.view.View
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.ScreenNavigator
import io.realm.Realm
import kotlinx.coroutines.*
import javax.inject.Inject

class LanguageListViewModel @Inject constructor(
	private val realm: Realm,
	private val screenNavigator: ScreenNavigator,
	private val languageRepository: LanguageRepository,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<LanguageListViewModel.Listener>() {
	interface Listener {
		fun onLanguagesLoaded(languages: List<LanguageRoom>)
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	lateinit var languages: List<LanguageRoom>

	fun fetchLanguages() {
		coroutineScope.launch(dispatcherProvider.main()) {
			withContext(dispatcherProvider.io()) {
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