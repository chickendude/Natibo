package ch.ralena.natibo.di.component

import ch.ralena.natibo.di.PresentationScope
import ch.ralena.natibo.ui.course.list.CourseListFragment
import dagger.Subcomponent

@PresentationScope
@Subcomponent
interface PresentationComponent {
	fun inject(fragment: CourseListFragment)
}