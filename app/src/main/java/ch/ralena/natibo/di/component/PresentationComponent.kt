package ch.ralena.natibo.di.component

import ch.ralena.natibo.di.PresentationScope
import ch.ralena.natibo.di.module.PresentationModule
import ch.ralena.natibo.ui.course.create.pick_language.PickLanguagesFragment
import ch.ralena.natibo.ui.course.create.pick_schedule.PickScheduleFragment
import ch.ralena.natibo.ui.course.detail.CourseDetailFragment
import ch.ralena.natibo.ui.course.list.CourseListFragment
import ch.ralena.natibo.ui.language.detail.LanguageDetailFragment
import ch.ralena.natibo.ui.language.list.LanguageListFragment
import ch.ralena.natibo.ui.study.insession.StudySessionFragment
import dagger.Subcomponent

@PresentationScope
@Subcomponent(modules = [PresentationModule::class])
interface PresentationComponent {
	fun inject(fragment: CourseListFragment)
	fun inject(fragment: PickLanguagesFragment)
	fun inject(fragment: PickScheduleFragment)
	fun inject(fragment: LanguageListFragment)
	fun inject(fragment: LanguageDetailFragment)
	fun inject(fragment: CourseDetailFragment)
	fun inject(fragment: StudySessionFragment)
}