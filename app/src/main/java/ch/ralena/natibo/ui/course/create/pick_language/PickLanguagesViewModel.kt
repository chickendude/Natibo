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
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<PickLanguagesViewModel.Listener>() {
	interface Listener {
		fun onLanguagesLoaded(languages: List<LanguageRoom>)
		fun onTargetLanguagesChanged(languages: List<LanguageRoom>)
		fun onPacksUpdated(packs: List<PackRoom>)
		fun onUpdateCheckMenuVisibility(isVisible: Boolean)
		fun onNativeLanguageChanged(language: LanguageRoom?)
		fun onTargetLanguageChanged(language: LanguageRoom?)
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())
	private lateinit var languagesWithPacks: List<LanguageWithPacks>
	private var nativeLanguage: LanguageRoom? = null
	private var targetLanguage: LanguageRoom? = null
	private var selectedPack: PackRoom? = null

	fun fetchLanguages() {
		coroutineScope.launch(dispatcherProvider.main()) {
			languagesWithPacks = withContext(dispatcherProvider.io()) {
				languageRepository.fetchLanguagesWithPacks()
			}
			val languages = languagesWithPacks.map { it.language }.sortedBy { it.name }
			listeners.forEach { it.onLanguagesLoaded(languages) }
		}
	}

	fun changeNativeLanguage(language: LanguageRoom) {
		nativeLanguage = language
		listeners.forEach { it.onNativeLanguageChanged(language) }

		// Clear the target language.
		changeTargetLanguage(null)
	}

	fun updateCheckMenuVisibility() {
		listeners.forEach {
			it.onUpdateCheckMenuVisibility(nativeLanguage != null && selectedPack != null)
		}
	}

	fun languagesConfirmed() {
		nativeLanguage?.let {
			// We may not have a target language set if the user is creating a monolingual course.
			val languageIds = listOfNotNull(it.id, targetLanguage?.id)
			screenNavigator.toCoursePreparationFragment(languageIds)
		}
	}

	fun changeTargetLanguage(language: LanguageRoom?) {
		targetLanguage = if (language == targetLanguage) null else language
		listeners.forEach { it.onTargetLanguageChanged(targetLanguage) }
		updateLanguagesAndPacks()
	}

	private fun updateLanguagesAndPacks() {
		// Find packs common to both languages.
		val nativePacks = languagesWithPacks.first { it.language == nativeLanguage }.packs
		val targetPacks = languagesWithPacks.firstOrNull { it.language == targetLanguage }?.packs

		// If no target language is set, show all packs from native language.
		val sharedPacks = nativePacks.filter { it in targetPacks ?: nativePacks }
		listeners.forEach { it.onPacksUpdated(sharedPacks) }

		// Grab list of languages with a matching pack.
		val possibleLanguages = languagesWithPacks
			.filter {
				it.packs.any { it in nativePacks }
			}.filterNot { it.language == nativeLanguage }
			.map { it.language }.sortedBy { it.name }
		listeners.forEach { it.onTargetLanguagesChanged(possibleLanguages) }

		updateCheckMenuVisibility()
	}
}