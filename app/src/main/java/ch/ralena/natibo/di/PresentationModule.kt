package ch.ralena.natibo.di

import android.media.MediaPlayer
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.ui.course.create.pick_language.adapter.NativeLanguagesAdapter
import ch.ralena.natibo.ui.course.create.pick_language.adapter.AvailablePacksAdapter
import ch.ralena.natibo.ui.course.create.pick_language.adapter.TargetLanguagesAdapter
import ch.ralena.natibo.ui.course.create.pick_schedule.adapter.PackAdapter
import ch.ralena.natibo.ui.course.list.adapter.CourseListAdapter
import ch.ralena.natibo.ui.language.detail.adapter.LanguageDetailAdapter
import ch.ralena.natibo.ui.language.list.adapter.LanguageListAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Qualifier

@Module
@InstallIn(FragmentComponent::class)
class PresentationModule {
	@FragmentScoped
	@Provides
	fun nativeLanguagesAdapter() = NativeLanguagesAdapter(arrayListOf())

	@FragmentScoped
	@Provides
	fun targetLanguagesAdapter() = TargetLanguagesAdapter(arrayListOf())

	@FragmentScoped
	@Provides
	fun selectedLanguagesAdapter() = AvailablePacksAdapter(arrayListOf())

	@FragmentScoped
	@Provides
	fun languageDetailAdapter() = LanguageDetailAdapter(arrayListOf())

	@FragmentScoped
	@Provides
	fun languageListAdapter() = LanguageListAdapter(arrayListOf())

	@FragmentScoped
	@Provides
	fun bookAdapter() = PackAdapter(arrayListOf(), arrayListOf())

	@FragmentScoped
	@Provides
	fun courseListAdapter() = CourseListAdapter(arrayListOf(), arrayListOf())

	@FragmentScoped
	@Provides
	@LanguageList
	fun selectedLanguages() = arrayListOf<LanguageRoom>()

	@FragmentScoped
	@Provides
	fun mediaPlayer() = MediaPlayer()
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class LanguageList

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SelectedLanguagesItemTouchHelper