package ch.ralena.natibo.di.module

import android.app.Application
import ch.ralena.natibo.di.AppScope
import dagger.Module
import dagger.Provides
import io.realm.Realm

@Module
class AppModule(private val application: Application) {
	@Provides
	fun realm(): Realm = Realm.getDefaultInstance()
}