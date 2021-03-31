package ch.ralena.natibo.di.module

import ch.ralena.natibo.di.PresentationScope
import ch.ralena.natibo.ui.course.create.adapter.AvailableLanguagesAdapter
import ch.ralena.natibo.ui.course.create.adapter.SelectedLanguagesAdapter
import dagger.Module
import dagger.Provides

@Module
class PresentationModule {
	@PresentationScope
	@Provides
	fun availableLanguagesAdapter() = AvailableLanguagesAdapter(arrayListOf(), arrayListOf())

	@PresentationScope
	@Provides
	fun selectedLanguagesAdapter() = SelectedLanguagesAdapter(arrayListOf(), )

}