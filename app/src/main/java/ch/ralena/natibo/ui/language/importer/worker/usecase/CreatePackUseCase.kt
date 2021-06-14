package ch.ralena.natibo.ui.language.importer.worker.usecase

import ch.ralena.natibo.data.room.PackRepository
import ch.ralena.natibo.data.room.`object`.PackRoom
import javax.inject.Inject

class CreatePackUseCase @Inject constructor(
	private val packRepository: PackRepository
) {
	suspend fun createPack(packName: String, languageId: Long): Long {
		var pack = packRepository.fetchPackByNameAndLanguage(packName, languageId)
		// Create the pack if it doesn't exist already
		if (pack == null) {
			pack = PackRoom(packName, languageId)
			return packRepository.createPack(pack)
		}
		return pack.id
	}
}