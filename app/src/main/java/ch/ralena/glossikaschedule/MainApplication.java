package ch.ralena.glossikaschedule;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;

// TODO: 12/30/2016 mark currently selected study day
public class MainApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		// initialize Realm
		Realm.init(this);

		Stetho.initialize(
				Stetho.newInitializerBuilder(this)
						.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
						.enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
						.build());

		RealmConfiguration config = new RealmConfiguration.Builder()
				.name("glossikaschedule.realm")
				.schemaVersion(1)
				.deleteRealmIfMigrationNeeded()
				.build();
		Realm.setDefaultConfiguration(config);
	}
}
