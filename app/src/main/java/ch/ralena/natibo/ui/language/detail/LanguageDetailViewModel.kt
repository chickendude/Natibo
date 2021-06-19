package ch.ralena.natibo.ui.language.detail

import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.*
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.ScreenNavigator
import io.realm.Realm
import kotlinx.coroutines.*
import javax.inject.Inject

class LanguageDetailViewModel @Inject constructor(
	private val languageRepository: LanguageRepository,
	private val screenNavigator: ScreenNavigator,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<LanguageDetailViewModel.Listener>() {
	interface Listener {
		fun onLanguageLoaded(languageWithPacks: LanguageWithPacks)
		fun onLanguageNotFound()
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	var languageWithPacks: LanguageWithPacks? = null

	fun loadLanguage(id: Long?) {
		if (id == null) {
			listeners.forEach { it.onLanguageNotFound() }
			return
		}

		coroutineScope.launch {
			languageWithPacks = languageRepository.fetchLanguageWithPacks(id)
			withContext(dispatcherProvider.main()) {
				languageWithPacks?.run {
					listeners.forEach { it.onLanguageLoaded(this) }
				} ?: run {
					listeners.forEach { it.onLanguageNotFound() }
				}
			}
		}
	}

	fun languagePackSelected(pack: PackRoom) {
		// todo notify invalid pack
		languageWithPacks?.run {
			screenNavigator.toSentenceListFragment(pack.id, language.code)
		}
	}
}