package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.room.`object`.Language
import io.realm.Realm
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
}