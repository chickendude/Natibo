package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.LanguageData
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.LanguagePackCrossRef
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.dao.LanguageDao
import io.realm.Realm
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class LanguageRepository @Inject constructor(
	private val languageDao: LanguageDao
) {
	suspend fun fetchLanguage(id: Long): LanguageRoom? {
		return languageDao.getById(id)
	}

	suspend fun fetchByCode(code: String): LanguageRoom? {
		return languageDao.getByCode(code)
	}

	suspend fun createLanguage(languageCode: String): Long? =
		// TODO: Create a new language if the ID isn't in LanguageData
		LanguageData.getLanguageById(languageCode)?.let {
			val language = LanguageRoom(it.name, languageCode, it.drawable)
			languageDao.insert(language)
		}

	suspend fun createLanguagePackCrossRef(languageId: Long, packId: Long) =
		languageDao.insertLanguagePackCrossRef(LanguagePackCrossRef(languageId, packId))

	suspend fun fetchLanguages(): List<LanguageRoom> =
		languageDao.getAll()

	suspend fun fetchLanguageWithPacks(id: Long) = languageDao.getByIdWithPacks(id)

	suspend fun fetchLanguagesWithPacks() = languageDao.getAllWithPacks()
}