package ch.ralena.natibo.di.module

import androidx.fragment.app.FragmentManager
import ch.ralena.natibo.di.ActivityScope
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.utils.DefaultDispatcherProvider
import ch.ralena.natibo.utils.DispatcherProvider
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(val activity: MainActivity) {
	@ActivityScope
	@Provides
	fun mainActivity(): MainActivity = activity

	@ActivityScope
	@Provides
	fun fragmentManager(): FragmentManager = activity.supportFragmentManager
}