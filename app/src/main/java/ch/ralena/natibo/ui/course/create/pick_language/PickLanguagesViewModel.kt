package ch.ralena.natibo.ui.course.create.pick_language

import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.LanguageWithPacks
import ch.ralena.natibo.data.room.`object`.PackRoom
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
		fun onPacksUpdated(packs: List<PackRoom>)
		fun onUpdateCheckMenuVisibility(isVisible: Boolean)
		fun onLanguageAdded(language: LanguageRoom)
		fun onLanguageRemoved(language: LanguageRoom)
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())
	private lateinit var languagesWithPacks: List<LanguageWithPacks>

	fun fetchLanguages() {
		coroutineScope.launch(dispatcherProvider.main()) {
			languagesWithPacks = withContext(dispatcherProvider.io()) {
				languageRepository.fetchLanguagesWithPacks()
			}
			val languages = languagesWithPacks.map { it.language }.sortedBy { it.name }
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

		// Find packs common to both languages
		val packs = languagesWithPacks
			.filter { it.language in selectedLanguages }
			.flatMap { it.packs }
			.groupBy { it }
			.filterValues { it.size == selectedLanguages.size }
			.keys
			.toList()
			.sortedBy { it.name }

		listeners.forEach { it.onPacksUpdated(packs) }
		val possibleLanguages = languagesWithPacks.filter {
			it.packs.any { it in packs } || packs.isEmpty()
		}.map { it.language }.sortedBy { it.name }
		listeners.forEach { it.onLanguagesLoaded(possibleLanguages) }

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