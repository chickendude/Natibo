package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.data.ScheduleType;

public class NewScheduleViewScheduleFragment extends Fragment {
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		// get schedule from passed in bundle
		ScheduleType schedule = getArguments().getParcelable(NewScheduleScheduleFragment.EXTRA_SCHEDULE);
		// load view
		View view = inflater.inflate(R.layout.fragment_new_schedule_view_schedule, container, false);
		// load views
		TextView title = view.findViewById(R.id.scheduleTitle);
		TextView minutes = view.findViewById(R.id.minutesLabel);
		TextView reps = view.findViewById(R.id.repsLabel);
		TextView monthsWeeks = view.findViewById(R.id.monthsWeeksLabel);
		TextView summary = view.findViewById(R.id.scheduleSummary);
		TextView description = view.findViewById(R.id.scheduleDescription);
		// update text
		title.setText(schedule.getTitle());
		minutes.setText(schedule.getMinutesDay() + "");
		reps.setText(schedule.getRepsAsString() + " reps");
		monthsWeeks.setText(schedule.getCourseLength() + " (" + schedule.getCourseLengthSmall() + ")");
		summary.setText(schedule.getSummary());
		description.setText(schedule.getDescription());

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.schedule_toolbar, menu);
	}
}
