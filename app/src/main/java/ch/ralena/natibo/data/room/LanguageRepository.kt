package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.LanguageData
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.dao.LanguageDao
import io.realm.Realm
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class LanguageRepository @Inject constructor(
	private val realm: Realm,
	private val languageDao: LanguageDao
) {
	suspend fun getById(id: String): LanguageRoom {
		return languageDao.getById(id)
	}

	suspend fun fetchByCode(code: String): LanguageRoom? {
		return languageDao.getByCode(code)
	}

	suspend fun createLanguage(id: String): Long? =
		// TODO: Create a new language if the ID isn't in LanguageData
		LanguageData.getLanguageById(id)?.let {
			val language = LanguageRoom(it.name, id, it.drawable)
			languageDao.insert(language)
		}

	suspend fun fetchLanguages(): List<LanguageRoom> =
		languageDao.getAll()

	suspend fun fetchLanguagesWithPacks() = languageDao.getAllWithPacks()

	fun fetchLanguagesSorted(): List<Language> {
		val languages = realm.where(Language::class.java).findAll()
		val languagesSorted = ArrayList<Language>()
		languagesSorted.addAll(languages)
		languagesSorted.sortWith(Comparator { lang1: Language, lang2: Language ->
			lang1.longName.compareTo(
				lang2.longName
			)
		})
		return languagesSorted
	}

	fun fetchLanguagesFromIds(languageIds: Array<String>): List<Language> {
		val languages = ArrayList<Language>()
		for (languageId in languageIds) {
			realm.where(Language::class.java).equalTo("languageId", languageId).findFirst()
				?.let { languages.add(it) }
		}
		return languages
	}

	fun setLanguages(course: Course, languages: List<Language>) {
		course.languages.clear()
		course.languages.addAll(languages)
	}
}