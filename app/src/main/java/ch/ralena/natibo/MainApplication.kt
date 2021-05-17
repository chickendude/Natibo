package ch.ralena.natibo

import android.app.Application
import ch.ralena.natibo.di.component.DaggerAppComponent
import ch.ralena.natibo.di.module.AppModule
import io.realm.Realm
import io.realm.RealmConfiguration

open class MainApplication : Application() {
	val appComponent by lazy {
		DaggerAppComponent.builder()
			.appModule(AppModule(this))
			.build()
	}

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