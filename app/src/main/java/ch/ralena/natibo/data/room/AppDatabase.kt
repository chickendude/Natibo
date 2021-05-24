package ch.ralena.natibo.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.data.room.dao.CourseDao
import ch.ralena.natibo.data.room.dao.PackDao
import ch.ralena.natibo.data.room.dao.SentenceDao

@Database(
	entities = [CourseRoom::class, PackRoom::class, SentenceRoom::class],
	version = 2
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun courseDao(): CourseDao
	abstract fun packDao(): PackDao
	abstract fun sentenceDao(): SentenceDao
}