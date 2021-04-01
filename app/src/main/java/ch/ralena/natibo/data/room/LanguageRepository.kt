package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import io.realm.Realm
import io.realm.RealmList
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class LanguageRepository @Inject constructor(
		private val realm: Realm
) {
	fun fetchLanguagesSorted(): List<Language> {
		val languages = realm.where(Language::class.java).findAll()
		val languagesSorted = ArrayList<Language>()
		languagesSorted.addAll(languages)
		languagesSorted.sortWith(Comparator { lang1: Language, lang2: Language -> lang1.longName.compareTo(lang2.longName) })
		return languagesSorted
	}

	fun fetchLanguagesFromIds(languageIds: Array<String>): List<Language> {
		val languages = ArrayList<Language>()
		for (languageId in languageIds) {
			realm.where(Language::class.java).equalTo("languageId", languageId).findFirst()?.let { languages.add(it) }
		}
		return languages
	}

	fun setLanguages(course: Course, languages: List<Language>) {
		course.languages.clear()
		course.languages.addAll(languages)
	}
}