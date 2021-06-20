package ch.ralena.natibo.ui.language.list

import android.view.View
import ch.ralena.natibo.data.room.LanguageRepository
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
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<LanguageListViewModel.Listener>() {
	interface Listener {
		fun onLanguagesLoaded(languagesWithPacks: List<LanguageWithPacks>)
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	lateinit var languagesWithPacks: List<LanguageWithPacks>

	fun fetchLanguages() {
		coroutineScope.launch(dispatcherProvider.main()) {
			withContext(dispatcherProvider.io()) {
				languagesWithPacks = languageRepository.fetchLanguagesWithPacks()
			}
			listeners.forEach { it.onLanguagesLoaded(languagesWithPacks) }
		}
	}

	fun languageSelected(language: LanguageRoom) {
		screenNavigator.toLanguageDetailsFragment(language.id)
	}

	fun getRecyclerViewVisibility(): Int =
		if (languagesWithPacks.isNullOrEmpty()) View.GONE else View.VISIBLE

	fun getNoCourseTextVisibility(): Int =
		if (languagesWithPacks.isNullOrEmpty()) View.VISIBLE else View.GONE
}