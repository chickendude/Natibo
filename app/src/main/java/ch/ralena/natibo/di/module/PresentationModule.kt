package ch.ralena.natibo.di.module

import android.media.MediaPlayer
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.di.PresentationScope
import ch.ralena.natibo.ui.course.create.pick_language.adapter.NativeLanguagesAdapter
import ch.ralena.natibo.ui.course.create.pick_language.adapter.AvailablePacksAdapter
import ch.ralena.natibo.ui.course.create.pick_language.adapter.TargetLanguagesAdapter
import ch.ralena.natibo.ui.course.create.pick_schedule.adapter.PackAdapter
import ch.ralena.natibo.ui.course.list.adapter.CourseListAdapter
import ch.ralena.natibo.ui.language.detail.adapter.LanguageDetailAdapter
import ch.ralena.natibo.ui.language.list.adapter.LanguageListAdapter
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Module
class PresentationModule {
	@PresentationScope
	@Provides
	fun nativeLanguagesAdapter() = NativeLanguagesAdapter(arrayListOf())

	@PresentationScope
	@Provides
	fun targetLanguagesAdapter() = TargetLanguagesAdapter(arrayListOf())

	@PresentationScope
	@Provides
	fun selectedLanguagesAdapter() = AvailablePacksAdapter(arrayListOf())

	@PresentationScope
	@Provides
	fun languageDetailAdapter() = LanguageDetailAdapter(arrayListOf())

	@PresentationScope
	@Provides
	fun languageListAdapter() = LanguageListAdapter(arrayListOf())

	@PresentationScope
	@Provides
	fun bookAdapter() = PackAdapter(arrayListOf(), arrayListOf())

	@PresentationScope
	@Provides
	fun courseListAdapter() = CourseListAdapter(arrayListOf(), arrayListOf())

	@PresentationScope
	@Provides
	@LanguageList
	fun selectedLanguages() = arrayListOf<LanguageRoom>()

	@PresentationScope
	@Provides
	fun mediaPlayer() = MediaPlayer()
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class LanguageList

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SelectedLanguagesItemTouchHelper