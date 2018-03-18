package ch.ralena.glossikaschedule;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import ch.ralena.glossikaschedule.adapter.DayAdapter;
import ch.ralena.glossikaschedule.adapter.NavigationAdapter;
import ch.ralena.glossikaschedule.fragment.MainFragment;
import ch.ralena.glossikaschedule.object.Schedule;
import io.realm.Realm;
import io.realm.RealmResults;

import static ch.ralena.glossikaschedule.NewScheduleActivity.EXTRA_NEW_SCHEDULE;

public class MainActivity extends AppCompatActivity implements DayAdapter.OnItemCheckedListener, NavigationAdapter.OnItemClickListener {
	private static final String TAG = MainActivity.class.getSimpleName();
	public static final String MAIN_FRAGMENT_TAG = "main_fragment";
	public static final String TAG_SCHEDULE_ID = "schedule_id";
	private static final String TAG_SCHEDULE_INDEX = "save_schedule_index";

	DrawerLayout drawerLayout;
	NavigationView navigationView;
	private FragmentManager fragmentManager;
	NavigationAdapter navigationAdapter;
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

		// if we don't have any schedules yet, request to create one, otherwise load the first schedule
		if (schedules.size() == 0) {
			loadNewScheduleActivity(false);
		} else {
			if(getIntent().getBooleanExtra(EXTRA_NEW_SCHEDULE, false)) {
				loadedSchedule = schedules.last();
			} else if (savedInstanceState != null) {
				int scheduleIndex = savedInstanceState.getInt(TAG_SCHEDULE_INDEX);
				loadedSchedule = schedules.get(scheduleIndex);
			} else {
				loadedSchedule = schedules.first();
			}
			loadMainFragment(loadedSchedule);
		}

		// set up nav drawer
		setupNavigationDrawer();
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
					return true;
				});
//		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.navigationRecyclerView);
//		navigationAdapter = new NavigationAdapter(this, schedules, schedules.indexOf(loadedSchedule));
//		recyclerView.setAdapter(navigationAdapter);
//		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
//		recyclerView.setLayoutManager(layoutManager);
	}

	private void loadMainFragment(Schedule schedule) {
		// update side drawer
		if (navigationAdapter != null) {
			int position = schedules.indexOf(schedule);
			navigationAdapter.setCurrentPosition(position);
			navigationAdapter.notifyDataSetChanged();
		}
		loadedSchedule = schedule;
		drawerLayout.closeDrawers();
		// load new fragment
		MainFragment mainFragment = new MainFragment();
		Bundle bundle = new Bundle();
		bundle.putString(TAG_SCHEDULE_ID, schedule.getId());
		mainFragment.setArguments(bundle);
		fragmentManager
				.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, mainFragment, MAIN_FRAGMENT_TAG)
				.commit();
	}

	private void loadNewScheduleActivity(boolean addToBackStack) {
		// close side drawer
		drawerLayout.closeDrawers();
		Intent intent = new Intent(this, NewScheduleActivity.class);
		// if this is the first time opening the app, there won't be any schedules so mainFragment won't have been created
		// therefore we shouldn't add the new schedule activity to the backstack
		MainFragment mainFragment = (MainFragment) fragmentManager.findFragmentByTag(MAIN_FRAGMENT_TAG);
		if (!addToBackStack) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		}
		startActivity(intent);
	}

	@Override
	public void onItemChecked() {
		MainFragment mainFragment = (MainFragment) fragmentManager.findFragmentByTag(MAIN_FRAGMENT_TAG);
		mainFragment.updateDay();
	}

	@Override
	public void onNewSchedule() {
		loadNewScheduleActivity(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
				return true;
			case R.id.action_delete:
				deleteSchedule();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void deleteSchedule() {
		final Snackbar snackbar = Snackbar.make(findViewById(R.id.fragmentPlaceHolder), "Delete " + loadedSchedule.getLanguage() + "?\n(Can't be undone!)", Snackbar.LENGTH_INDEFINITE);
		snackbar.setAction("Delete", view -> {
			// find array index of the currently loaded schedule
			int position = schedules.indexOf(loadedSchedule);

			// delete the schedule
			realm.executeTransaction(r -> {
				schedules.deleteFromRealm(position);
			});

			// update the side menu
			navigationAdapter.notifyItemRemoved(position);

			// load the next schedule if there's one left
			int newPosition = position > 0 ? position - 1 : position;
			if (schedules.size() > 0) {
				loadMainFragment(schedules.get(newPosition));
			} else {
				loadNewScheduleActivity(false);
			}
			snackbar.dismiss();
		});
		snackbar.show();
	}

	@Override
	public void onScheduleClicked(Schedule schedule) {
		loadMainFragment(schedule);
	}
}
