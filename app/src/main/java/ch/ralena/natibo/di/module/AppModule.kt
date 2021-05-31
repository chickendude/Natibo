package ch.ralena.natibo.di.module

import android.app.Application
import android.content.ContentResolver
import androidx.room.Room
import ch.ralena.natibo.data.room.AppDatabase
import ch.ralena.natibo.di.AppScope
import dagger.Module
import dagger.Provides
import io.realm.Realm

@Module
class AppModule(private val application: Application) {
	@Provides
	@AppScope
	fun contentResolver(): ContentResolver = application.contentResolver

	@Provides
	fun realm(): Realm = Realm.getDefaultInstance()

	@Provides
	@AppScope
	fun database(): AppDatabase = Room.databaseBuilder(
		application,
		AppDatabase::class.java,
		"natibo_db"
	).fallbackToDestructiveMigration()
		.build()

	@Provides
	@AppScope
	fun courseDao(database: AppDatabase) = database.courseDao()

	@Provides
	@AppScope
	fun packDao(database: AppDatabase) = database.packDao()

	@Provides
	@AppScope
	fun sentenceDao(database: AppDatabase) = database.sentenceDao()

	@Provides
	@AppScope
	fun languageDao(database: AppDatabase) = database.languageDao()
}