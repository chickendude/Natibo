package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import ch.ralena.glossikaschedule.NewScheduleActivity;
import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.data.ScheduleData;
import ch.ralena.glossikaschedule.data.ScheduleType;

public class NewScheduleScheduleFragment extends Fragment {
	private static final String TAG = NewScheduleScheduleFragment.class.getSimpleName();
	public static final String EXTRA_SCHEDULE = "extra_schedule";

	private LinearLayout circleContainer;
	private LayoutInflater inflater;
	private List<View> minuteCircles;
	private TreeMap<Integer, List<ScheduleType>> schedules;
	NewScheduleActivity activity;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		activity = (NewScheduleActivity) getActivity();
		ActionBar actionBar = activity.getSupportActionBar();
		actionBar.setTitle("Choose a schedule");
		actionBar.setDisplayHomeAsUpEnabled(true);

		// save inflater
		this.inflater = inflater;

		// organize schedules by how many minutes they take
		schedules = new TreeMap<>();
		for (ScheduleType scheduleType : ScheduleData.scheduleList) {
			if (!schedules.containsKey(scheduleType.getMinutesDay())) {
				schedules.put(scheduleType.getMinutesDay(), new ArrayList<>());
			}
			schedules.get(scheduleType.getMinutesDay()).add(scheduleType);
		}

		// load view
		View view = inflater.inflate(R.layout.fragment_new_schedule_schedule, container, false);

		circleContainer = view.findViewById(R.id.circleContainer);

		loadMinuteCircles();
		return view;
	}

	private void loadMinuteCircles() {
		minuteCircles = new ArrayList<>();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 10, 0, 10);
		for (Integer minutes : schedules.keySet()) {
			// inflate view
			View minuteCircleView = inflater.inflate(R.layout.item_schedule_minutes, circleContainer, false);
			minuteCircleView.setLayoutParams(lp);
			// update minutes text
			TextView minutesLabel = minuteCircleView.findViewById(R.id.minutesLabel);
			minutesLabel.setText(minutes + "");
			// load descriptions
			FlexboxLayout descriptionContainer = minuteCircleView.findViewById(R.id.descriptionContainer);
			loadScheduleDescriptions(descriptionContainer, minutes);

			// add to arraylist and to layout
			minuteCircles.add(minuteCircleView);
			circleContainer.addView(minuteCircleView);
		}
	}

	private void loadScheduleDescriptions(FlexboxLayout descriptionContainer, int minutes) {
		int index = 0;
		for (ScheduleType scheduleType : schedules.get(minutes)) {
			View descriptionView = inflater.inflate(R.layout.item_schedule_description, descriptionContainer, false);
			// set up onclick listener
			LinearLayout scheduleLayout = descriptionView.findViewById(R.id.scheduleLayout);
			scheduleLayout.setOnClickListener(view -> {
				loadScheduleDetails(scheduleType);
			});
			// update textviews
			TextView monthsLabel = descriptionView.findViewById(R.id.bigLengthLabel);
			TextView weeksLabel = descriptionView.findViewById(R.id.smallLengthLabel);
			TextView repsLabel = descriptionView.findViewById(R.id.repsLabel);
			monthsLabel.setText(scheduleType.getCourseLength());
			weeksLabel.setText(scheduleType.getCourseLengthSmall());
			repsLabel.setText(scheduleType.getRepsAsString());
			// if it's the last one, don't show the "or"
			if (++index == schedules.get(minutes).size()) {
				TextView orLabel = descriptionView.findViewById(R.id.orLabel);
				orLabel.setVisibility(View.GONE);
			}
			// add view to container
			descriptionContainer.addView(descriptionView);
		}
	}

	private void loadScheduleDetails(ScheduleType scheduleType) {
		activity.updateSchedule(scheduleType);
		NewScheduleViewScheduleFragment fragment = new NewScheduleViewScheduleFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(EXTRA_SCHEDULE, scheduleType);
		fragment.setArguments(bundle);

		getFragmentManager().beginTransaction()
				.replace(R.id.fragmentContainer, fragment)
				.addToBackStack(null)
				.commit();
	}
}
