package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.ralena.glossikaschedule.MainActivity;
import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.adapter.ScheduleAdapter;
import ch.ralena.glossikaschedule.object.Day;
import ch.ralena.glossikaschedule.object.Schedule;
import ch.ralena.glossikaschedule.object.StudyItem;
import io.realm.Realm;
import io.realm.RealmList;

// TODO: Long press with popup menu to ask to fill in all previous days
public class MainFragment extends Fragment {
	private static final String TAG = MainFragment.class.getSimpleName();
	public static final String TAG_DAY_ID = "tag_day_id";
	private static final String DAY_FRAGMENT_TAG = "day_fragment";

	private Schedule schedule;
	private int currentDayId = -1;
	private Day currentDay;
	private ScheduleAdapter adapter;
	private View rootView;
	private boolean isDialogReady;
	private Snackbar snackbar;

	Realm realm;

	DayFragment dayFragment;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		isDialogReady = true;

		// initialize realm object
		realm = Realm.getDefaultInstance();

		// get the arguments passed in
		Bundle bundle = getArguments();
		String id = bundle.getString(MainActivity.TAG_SCHEDULE_ID);
		schedule = realm.where(Schedule.class).equalTo("id", id).findFirst();

		// set up title
		((MainActivity) getActivity()).getSupportActionBar().setTitle(schedule.getLanguage() + " - " + schedule.getTitle());

		// load views
		rootView = inflater.inflate(R.layout.fragment_main, container, false);

		// skip over all days that have already been completed
		findNextIncompleteDay();

		// set up adapter and subscribe to clicks on a day
		int currentDayPosition = schedule.getSchedule().indexOf(currentDay);
		adapter = new ScheduleAdapter(currentDayPosition, schedule.getSchedule(), getContext());
		adapter.asObservable().subscribe(this::showDay);

		// set up recycler view
		RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.scheduleRecyclerView);
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 7);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.smoothScrollToPosition(currentDayPosition);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d("TAG", "onresume");
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// if it's the first time loading the fragment, show the current day
		if (savedInstanceState == null) {
			showDay(currentDay);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (dayFragment != null && !isStateSaved()) {
			dayFragment.dismiss();
		}
	}

	// loads a dialog fragment with checkboxes for the recordings you need to study for that day
	public void showDay(Day day) {
		currentDay = day;
		adapter.notifyItemChanged(schedule.getSchedule().indexOf(day));
		if (isDialogReady) {
			if (areEmptyDays()) {
				askToFillInDays();
			} else {
				openDayDialog();
			}
		} else {
			if (areEmptyDays()) {
				snackbar.setText(String.format(getString(R.string.mark_days_as_complete), currentDay.getDayNumber()));
			} else {
				snackbar.dismiss();
				openDayDialog();
			}
		}
	}

	public void updateAdapter() {
		adapter.notifyDataSetChanged();
	}

	// creates the dialog fragment with the day's files
	private void openDayDialog() {
		if (getFragmentManager() != null) {
			dayFragment = (DayFragment) getFragmentManager().findFragmentByTag(DAY_FRAGMENT_TAG);
			if (dayFragment == null) {
				dayFragment = new DayFragment();
				dayFragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.dialog);
				Bundle bundle = new Bundle();
				bundle.putString(TAG_DAY_ID, currentDay.getId());
				dayFragment.setArguments(bundle);
				dayFragment.show(getFragmentManager(), DAY_FRAGMENT_TAG);
			}
		}
	}

	// fill in all days from start to current day
	private void askToFillInDays() {
		isDialogReady = false;
		snackbar = Snackbar.make(rootView,
				String.format(getString(R.string.mark_days_as_complete), currentDay.getDayNumber()),
				Snackbar.LENGTH_INDEFINITE)
				.setAction("Yes", v -> {
					for (Day day : schedule.getSchedule()) {
						if (day.getDayNumber() < currentDay.getDayNumber()) {
							for (StudyItem studyItem : day.getStudyItems()) {
								realm.executeTransaction(r -> studyItem.setCompleted(true));
							}
							realm.executeTransaction(r -> {
										day.setCompleted(true);
										day.updateDateCompleted();
									}
							);
						}
					}
					adapter.notifyDataSetChanged();
				}).addCallback(new Snackbar.Callback() {
					@Override
					public void onDismissed(Snackbar transientBottomBar, int event) {
						super.onDismissed(transientBottomBar, event);
						isDialogReady = true;
						openDayDialog();
					}
				});
		snackbar.show();
	}

	private boolean areEmptyDays() {
		RealmList<Day> days = schedule.getSchedule();
		boolean isEmpty = false;
		if (currentDay != null) {
			int currentDayIndex = currentDay.getDayNumber();
			for (Day day : days) {
				if (day.getDayNumber() < currentDayIndex && !day.isCompleted())
					isEmpty = true;
			}
		}
		return isEmpty;
	}

	private void findNextIncompleteDay() {
		for (Day day : schedule.getSchedule()) {
			currentDayId = day.getDayNumber() - 1;
			if (!day.isCompleted()) {
				getCurrentDay();
				return;
			}
		}
	}

	public Day getCurrentDay() {
		int index = currentDayId;
		if (index < 0) {
			index = 0;
		}
		currentDay = schedule.getSchedule().get(index);
		return currentDay;
	}

	public void updateDay() {
		// check if all items have been checked
		boolean isCompleted = true;
		int numberCompleted = 0;
		int total = currentDay.getStudyItems().size();
		for (StudyItem studyItem : currentDay.getStudyItems()) {
			if (studyItem.isCompleted()) {
				numberCompleted++;
			}
			isCompleted = isCompleted & studyItem.isCompleted();
		}
		boolean finalIsCompleted = isCompleted;
		realm.executeTransaction(r -> currentDay.setCompleted(finalIsCompleted));

		// if all items have been checked or all but one have been checked, update day's background
		if (numberCompleted == total || numberCompleted == total - 1) {
			int position = schedule.getSchedule().indexOf(currentDay);
			adapter.notifyItemChanged(position);
		}
	}

	public void removeHighlight() {
		adapter.removeHighlight();
	}

}
