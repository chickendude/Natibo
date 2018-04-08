package ch.ralena.glossikaschedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import ch.ralena.glossikaschedule.fragment.LanguageImportFragment;
import ch.ralena.glossikaschedule.fragment.LanguageListFragment;
import ch.ralena.glossikaschedule.object.Schedule;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String TAG_SCHEDULE_INDEX = "save_schedule_index";
	private static final int REQUEST_PICK_GLS = 1;

	DrawerLayout drawerLayout;
	NavigationView navigationView;
	private FragmentManager fragmentManager;
	ActionBarDrawerToggle drawerToggle;

	RealmResults<Schedule> schedules;
	Schedule loadedSchedule;

	private Realm realm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		drawerLayout = findViewById(R.id.drawerLayout);
		navigationView = findViewById(R.id.navigationView);

		// set up toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		fragmentManager = getSupportFragmentManager();

		// load schedules from database
		realm = Realm.getDefaultInstance();
		schedules = realm.where(Schedule.class).findAll();

		// set up nav drawer
		setupNavigationDrawer();
		loadLanguageListFragment();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		int scheduleIndex = loadedSchedule == null ? 0 : schedules.indexOf(loadedSchedule);
		outState.putInt(TAG_SCHEDULE_INDEX, scheduleIndex);
	}

	private void setupNavigationDrawer() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
			}
		};
		drawerToggle.syncState();
		drawerToggle.setDrawerIndicatorEnabled(true);
		drawerLayout.addDrawerListener(drawerToggle);

		navigationView.setNavigationItemSelectedListener(
				menuItem -> {
					menuItem.setCheckable(true);
					navigationView.setCheckedItem(menuItem.getItemId());
					drawerLayout.closeDrawers();

					switch (menuItem.getItemId()) {
						case R.id.nav_languages:
							loadLanguageListFragment();
							break;
						case R.id.nav_new_schedule:
							loadNewScheduleActivity();
							break;
						case R.id.nav_settings:
							break;
						case R.id.nav_import:
							importLanguagPack();
							break;
					}

					return true;
				});
		navigationView.setCheckedItem(R.id.nav_languages);
	}

	private void importLanguagPack() {
		Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT);
		mediaIntent.setType("application/*");
		startActivityForResult(mediaIntent, REQUEST_PICK_GLS);
	}

	private void loadNewScheduleActivity() {
		// close side drawer
		drawerLayout.closeDrawers();
		Intent intent = new Intent(this, NewScheduleActivity.class);
		startActivity(intent);
	}

	private void loadLanguageListFragment() {
		LanguageListFragment fragment = new LanguageListFragment();
		fragmentManager
				.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_PICK_GLS && resultCode == Activity.RESULT_OK) {
			LanguageImportFragment fragment = new LanguageImportFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable(LanguageImportFragment.EXTRA_URI, data.getData());
			fragment.setArguments(bundle);

			fragmentManager.beginTransaction()
					.replace(R.id.fragmentPlaceHolder, fragment)
					.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setNavigationDrawerItemChecked(int itemNum) {
		navigationView.setCheckedItem(itemNum);
	}
}
