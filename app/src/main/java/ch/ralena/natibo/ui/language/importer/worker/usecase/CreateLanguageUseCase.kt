package ch.ralena.natibo.ui.language.importer.worker.usecase

import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.ui.language.importer.worker.ImportException
import javax.inject.Inject

class CreateLanguageUseCase @Inject constructor(
	private val languageRepository: LanguageRepository
) {
	suspend fun fetchOrCreateLanguage(languageCode: String): Long {
		// Check if the language has already been created. If it hasn't, create it.
		// If no language was found and it failed to create a new one, an exception is thrown.
		val language = languageRepository.fetchByCode(languageCode)
		val languageId = language?.id ?: languageRepository.createLanguage(languageCode)
		return languageId ?: throw ImportException("Unable to create language with id: $languageCode")
	}
}