package ch.ralena.glossikaschedule;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import ch.ralena.glossikaschedule.fragment.CourseListFragment;
import ch.ralena.glossikaschedule.fragment.LanguageImportFragment;
import ch.ralena.glossikaschedule.fragment.LanguageListFragment;
import ch.ralena.glossikaschedule.fragment.StudySessionFragment;
import ch.ralena.glossikaschedule.object.Day;
import ch.ralena.glossikaschedule.service.StudySessionService;
import ch.ralena.glossikaschedule.utils.Utils;
import io.reactivex.subjects.PublishSubject;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String KEY_SERVICE_BOUND = "key_service_bound";
	public static final String ACTION_START_SESSION = "action_start_session";
	private static final int ACTION_OPEN_DRAWER = 0;
	private static final int ACTION_BACK = 1;
	private static final int REQUEST_PICK_GLS = 1;
	public static final int REQUEST_LOAD_SESSION = 2;

	// views
	DrawerLayout drawerLayout;
	NavigationView navigationView;
	private FragmentManager fragmentManager;
	ActionBarDrawerToggle drawerToggle;

	// fields
	int homeAction;
	private StudySessionService studySessionService;
	private boolean isServiceBound = false;

	private Realm realm;

	private PublishSubject<StudySessionService> sessionPublish = PublishSubject.create();

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			StudySessionService.StudyBinder binder = (StudySessionService.StudyBinder) service;
			studySessionService = ((StudySessionService.StudyBinder) service).getService();
			isServiceBound = true;
			// publish the service object
			sessionPublish.onNext(studySessionService);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			isServiceBound = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		homeAction = ACTION_OPEN_DRAWER;

		drawerLayout = findViewById(R.id.drawerLayout);
		navigationView = findViewById(R.id.navigationView);

		// set up toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		fragmentManager = getSupportFragmentManager();

		// load schedules from database
		realm = Realm.getDefaultInstance();

		// set up nav drawer
		setupNavigationDrawer();
		loadCourseListFragment();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_SERVICE_BOUND, isServiceBound);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		isServiceBound = savedInstanceState.getBoolean(KEY_SERVICE_BOUND, false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isServiceBound) {
			unbindService(serviceConnection);
			studySessionService.removeNotification();
			studySessionService.stopSelf();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isServiceBound) {
			studySessionService.removeNotification();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isServiceBound) {
			studySessionService.buildNotification();
		}
	}

	/**
	 * Setup the Action Bar, the hamburger button to toggle the navigation drawer, and prepare the
	 * navigation view and act on menu selections.
	 */
	private void setupNavigationDrawer() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
//				super.onDrawerOpened(drawerView);
//				invalidateOptionsMenu();
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
						case R.id.nav_courses:
							loadCourseListFragment();
							break;
						case R.id.nav_settings:
							break;
						case R.id.nav_import:
							importLanguagPack();
							break;
					}

					return true;
				});
		navigationView.setCheckedItem(R.id.nav_courses);
	}

	/**
	 * Opens a file browser to search for a language pack to import into the app.
	 */
	public void importLanguagPack() {
		clearBackStack();
		Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT);
		mediaIntent.setType("application/*");
		startActivityForResult(mediaIntent, REQUEST_PICK_GLS);
	}

	/**
	 * Loads the LanguageListFragment fragment.
	 */
	private void loadLanguageListFragment() {
		clearBackStack();
		LanguageListFragment fragment = new LanguageListFragment();
		fragmentManager
				.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.commit();
	}

	/**
	 * Loads the CourseListFragment fragment.
	 */
	public void loadCourseListFragment() {
		loadCourseListFragment(null);
	}

	public void loadCourseListFragment(String courseId) {
		clearBackStack();
		CourseListFragment fragment = new CourseListFragment();
		if (courseId != null) {
			Bundle bundle = new Bundle();
			bundle.putString(CourseListFragment.TAG_COURSE_ID, courseId);
			fragment.setArguments(bundle);
		}
		fragmentManager
				.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.commit();
	}

	private void clearBackStack() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			int entryId = getSupportFragmentManager().getBackStackEntryAt(0).getId();
			getSupportFragmentManager().popBackStackImmediate(entryId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_PICK_GLS) {
				LanguageImportFragment fragment = new LanguageImportFragment();
				Bundle bundle = new Bundle();
				bundle.putParcelable(LanguageImportFragment.EXTRA_URI, data.getData());
				fragment.setArguments(bundle);

				fragmentManager.beginTransaction()
						.replace(R.id.fragmentPlaceHolder, fragment)
						.commit();
			} else if (requestCode == REQUEST_LOAD_SESSION) {
				StudySessionFragment fragment = new StudySessionFragment();
				fragmentManager
						.beginTransaction()
						.replace(R.id.fragmentPlaceHolder, fragment)
						.commit();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				homeButtonPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void homeButtonPressed() {
		switch (homeAction) {
			case ACTION_OPEN_DRAWER:
				drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
				break;
			case ACTION_BACK:
				onBackPressed();
				break;
		}
	}

	// public methods

	public void setNavigationDrawerItemChecked(int itemNum) {
		navigationView.setCheckedItem(itemNum);
	}

	public void enableBackButton() {
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		drawerToggle.setDrawerIndicatorEnabled(false);
		homeAction = ACTION_BACK;
	}

	public void enableHomeButton() {
		drawerToggle.setDrawerIndicatorEnabled(true);
		homeAction = ACTION_OPEN_DRAWER;
	}

	public void snackBar(int stringId) {
		snackBar(getString(stringId));
	}

	public void snackBar(String message) {
		Snackbar snackbar = Snackbar.make(findViewById(R.id.fragmentPlaceHolder), message, Snackbar.LENGTH_INDEFINITE);
		snackbar.setAction(R.string.ok, v -> snackbar.dismiss());
		snackbar.show();
	}

	// --- study session service methods ---

	public void startSession(Day day) {
		Utils.Storage storage = new Utils.Storage(this);
		storage.putDayId(day.getId());

		if (!isServiceBound) {
			Intent intent = new Intent(this, StudySessionService.class);
			startService(intent);
			bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		} else {
			Intent intent = new Intent(StudySessionService.BROADCAST_START_SESSION);
			sendBroadcast(intent);
		}
	}

	public PublishSubject<StudySessionService> getSessionPublish() {
		return sessionPublish;
	}
}
