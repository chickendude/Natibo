package ch.ralena.glossikaschedule.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.adapter.DayAdapter;
import ch.ralena.glossikaschedule.object.Day;
import io.realm.Realm;

import static ch.ralena.glossikaschedule.MainActivity.MAIN_FRAGMENT_TAG;

public class DayFragment extends DialogFragment {
	private Realm realm;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		getDialog().setCanceledOnTouchOutside(true);

		realm = Realm.getDefaultInstance();

		// get day from passed in bundle
		Bundle bundle = getArguments();
		String dayId = bundle.getString(MainFragment.TAG_DAY_ID);
		Day day = realm.where(Day.class).equalTo("id", dayId).findFirst();

		// inflate views
		View view = inflater.inflate(R.layout.fragment_day, container, false);
		TextView dayLabel = view.findViewById(R.id.dayLabel);
		final CheckBox checkAll = view.findViewById(R.id.checkAll);

		// set up day title
		dayLabel.setText("Day " + day.getDayNumber());

		// set up completion date
		LinearLayout completedDateLayout = view.findViewById(R.id.completedDateLayout);
		if (day.getFormattedDateCompleted().equals("")) {
			// hide layout if it hasn't been completed yet
			completedDateLayout.setVisibility(View.GONE);
		} else {
			TextView completedDateText = view.findViewById(R.id.completedDateText);
			// if date completed = 1, it was converted over from an older version
			// before we kept track of completion date
			if (day.getDateCompleted() == 1) {
				completedDateText.setText("---");
			} else {
				// otherwise update text to show the date it was completed
				completedDateText.setText(day.getFormattedDateCompleted());
			}
		}

		// set up recycler view and adapter
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
		final DayAdapter adapter = new DayAdapter(day, (DayAdapter.OnItemCheckedListener) getActivity());
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(layoutManager);

		// set up check all listener
		checkAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
			checkAll.setChecked(false);
			adapter.changeAll(isChecked);

			// if they've all been checked, update the day's completion date
			realm.executeTransaction(r -> day.updateDateCompleted());

			// we need to wait a short while, otherwise the changes won't get marked
			new Handler().postDelayed(this::dismiss, 400);
		});

		return view;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (getFragmentManager() != null) {
			MainFragment mainFragment = (MainFragment) getFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
			if (mainFragment != null) {
				mainFragment.removeHighlight();
				mainFragment.updateAdapter();
			}
		}
		super.onDismiss(dialog);
	}
}
