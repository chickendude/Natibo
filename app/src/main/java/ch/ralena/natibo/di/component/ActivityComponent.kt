package ch.ralena.natibo.di.component

import ch.ralena.natibo.di.ActivityScope
import ch.ralena.natibo.di.module.ActivityModule
import ch.ralena.natibo.ui.MainActivity
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {
	fun newPresentationComponent(): PresentationComponent

	fun inject(activity: MainActivity)
}