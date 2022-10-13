package ch.ralena.natibo.ui.language.importer.worker.usecase

import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.PackRepository
import ch.ralena.natibo.data.room.`object`.PackRoom
import javax.inject.Inject

class CreatePackUseCase @Inject constructor(
	private val packRepository: PackRepository,
	private val languageRepository: LanguageRepository
) {
	suspend fun createPack(packName: String, languageId: Long): Long {
		var pack = packRepository.fetchPackByName(packName)
		var packId = pack?.id
		// Create the pack if it doesn't exist already
		if (packId == null) {
			pack = PackRoom(packName)
			packId = packRepository.createPack(pack)
		}
		languageRepository.createLanguagePackCrossRef(languageId, packId)
		return packId
	}
}