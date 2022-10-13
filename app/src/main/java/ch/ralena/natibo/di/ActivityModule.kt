package ch.ralena.natibo.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import ch.ralena.natibo.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {
	@ActivityScoped
	@Provides
	fun mainActivity(@ActivityContext activity: Context): MainActivity = activity as MainActivity

	@ActivityScoped
	@Provides
	fun fragmentManager(@ActivityContext activity: Context): FragmentManager =
		(activity as AppCompatActivity).supportFragmentManager
}