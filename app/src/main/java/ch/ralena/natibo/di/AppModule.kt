package ch.ralena.natibo.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import ch.ralena.natibo.data.room.AppDatabase
import ch.ralena.natibo.utils.DefaultDispatcherProvider
import ch.ralena.natibo.utils.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
	@Provides
	@Singleton
	fun contentResolver(@ApplicationContext appContext: Context): ContentResolver =
		appContext.contentResolver

	@Provides
	@Singleton
	fun dispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

	@Provides
	@Singleton
	fun database(@ApplicationContext appContext: Context): AppDatabase = Room.databaseBuilder(
		appContext,
		AppDatabase::class.java,
		"natibo_db"
	).fallbackToDestructiveMigration()
		.build()

	@Provides
	@Singleton
	fun courseDao(database: AppDatabase) = database.courseDao()

	@Provides
	@Singleton
	fun languageDao(database: AppDatabase) = database.languageDao()

	@Provides
	@Singleton
	fun packDao(database: AppDatabase) = database.packDao()

	@Provides
	@Singleton
	fun sentenceDao(database: AppDatabase) = database.sentenceDao()

	@Provides
	@Singleton
	fun sesseionDao(database: AppDatabase) = database.sessionDao()
}