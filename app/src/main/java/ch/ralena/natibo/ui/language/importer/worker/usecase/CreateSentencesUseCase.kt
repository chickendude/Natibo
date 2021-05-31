package ch.ralena.natibo.ui.language.importer.worker.usecase

import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.ui.language.importer.worker.PackImporterWorker
import ch.ralena.natibo.utils.Utils.readZip
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

class CreateSentencesUseCase @Inject constructor() {
	suspend fun createSentences(languageId: Long, packId: Long, sentences: List<String>) {

		// --- begin transaction
//		realm.beginTransaction()
//
//		// create base language and pack if they don't exist
//		var base = realm.where(
//			Language::class.java
//		).equalTo("languageId", baseLanguage).findFirst()
//		if (base == null) {
//			base = realm.createObject(Language::class.java, baseLanguage)
//		}
//		var basePack: Pack? = base!!.getPack(packName)
//		if (basePack == null) {
//			basePack = realm.createObject<Pack>(Pack::class.java, UUID.randomUUID().toString())
//			basePack.setBook(packName)
//			base.packs.add(basePack)
//		}
//
//		// create target language and pack if they don't exist
//		var target: Language?
//		var targetPack: Pack? = null
//		if (targetLanguage != "") {
//			target =
//				realm.where(Language::class.java).equalTo("languageId", targetLanguage).findFirst()
//			if (target == null) {
//				target = realm.createObject(Language::class.java, targetLanguage)
//			}
//			targetPack = target!!.getPack(packName)
//			if (targetPack == null) {
//				targetPack =
//					realm.createObject<Pack>(Pack::class.java, UUID.randomUUID().toString())
//				targetPack.setBook(packName)
//				target.packs.add(targetPack)
//			}
//		}
//		realm.commitTransaction()
//		// --- end transaction
	}

	// region Helper functions----------------------------------------------------------------------
	/**
	 * Reads the .gsp file and returns the list of sentences from it.
	 *
	 * @param zis ZipInputStream positioned at the .gsp file.
	 * @return List of strings containing the lines from the .gsp file.
	 */
	private fun readGspFile(zis: ZipInputStream): List<String> {
		val buffer = ByteArray(PackImporterWorker.BUFFER_SIZE)
		var numBytesRead: Int
		val baos = ByteArrayOutputStream()
		while (
			zis.read(
				buffer,
				0,
				PackImporterWorker.BUFFER_SIZE
			).also { numBytesRead = it } >= 0
		) {
			baos.write(buffer, 0, numBytesRead)
		}
		val text = baos.toString("UTF-8").trim('\n')
		return text.split("\n")
	}
	// endregion Helper functions-------------------------------------------------------------------
}