package ch.ralena.glossikaschedule;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

// TODO: 12/30/2016 mark currently selected study day
public class MainApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		// initialize Realm
		Realm.init(this);
		RealmConfiguration config = new RealmConfiguration.Builder()
				.name("glossikaschedule.realm")
				.schemaVersion(1)
				.deleteRealmIfMigrationNeeded()
				.build();
		Realm.setDefaultConfiguration(config);
	}
}
