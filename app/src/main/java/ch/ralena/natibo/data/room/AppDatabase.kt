package ch.ralena.natibo.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.dao.CourseDao

@Database(
	entities = [CourseRoom::class],
	version = 2
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun courseDao(): CourseDao
}