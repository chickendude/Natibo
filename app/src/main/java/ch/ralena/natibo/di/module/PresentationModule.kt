package ch.ralena.natibo.di.module

import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.di.PresentationScope
import ch.ralena.natibo.ui.course.create.adapter.AvailableLanguagesAdapter
import ch.ralena.natibo.ui.course.create.adapter.SelectedLanguagesAdapter
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Module
class PresentationModule {
	@PresentationScope
	@Provides
	fun availableLanguagesAdapter() = AvailableLanguagesAdapter(arrayListOf(), arrayListOf())

	@PresentationScope
	@Provides
	fun selectedLanguagesAdapter() = SelectedLanguagesAdapter(arrayListOf())

	@PresentationScope
	@Provides
	@SelectedLanguages
	fun selectedLanguages() = arrayListOf<Language>()
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SelectedLanguages