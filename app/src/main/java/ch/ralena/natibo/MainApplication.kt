package ch.ralena.natibo

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import ch.ralena.natibo.di.AppModule
import dagger.hilt.android.HiltAndroidApp
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

@HiltAndroidApp
open class MainApplication : Application(), Configuration.Provider {
	@Inject
	lateinit var workerFactory: HiltWorkerFactory

	override fun getWorkManagerConfiguration() =
		Configuration.Builder()
			.setWorkerFactory(workerFactory)
			.build()

	override fun onCreate() {
		super.onCreate()
		// initialize Realm
		Realm.init(this)
		val config = RealmConfiguration.Builder()
			.name("natibo.realm")
			.allowWritesOnUiThread(true)
			.schemaVersion(1)
			.deleteRealmIfMigrationNeeded()
			.build()
		Realm.setDefaultConfiguration(config)
	}
}