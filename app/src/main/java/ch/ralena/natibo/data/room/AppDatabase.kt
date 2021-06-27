package ch.ralena.natibo.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.ralena.natibo.data.room.`object`.*
import ch.ralena.natibo.data.room.dao.*

@Database(
	entities = [
		CourseRoom::class,
		LanguageRoom::class,
		PackRoom::class,
		SentenceRoom::class,
		SessionRoom::class,
		SessionSentenceCrossRef::class
	],
	version = 1
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun courseDao(): CourseDao
	abstract fun languageDao(): LanguageDao
	abstract fun packDao(): PackDao
	abstract fun sentenceDao(): SentenceDao
	abstract fun sessionDao(): SessionDao
}