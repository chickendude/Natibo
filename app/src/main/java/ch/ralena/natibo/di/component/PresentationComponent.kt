package ch.ralena.natibo.di.component

import ch.ralena.natibo.di.PresentationScope
import ch.ralena.natibo.di.module.PresentationModule
import ch.ralena.natibo.ui.course.create.pick_language.PickLanguagesFragment
import ch.ralena.natibo.ui.course.list.CourseListFragment
import dagger.Subcomponent

@PresentationScope
@Subcomponent(modules = [PresentationModule::class])
interface PresentationComponent {
	fun inject(fragment: CourseListFragment)
	fun inject(fragment: PickLanguagesFragment)
}