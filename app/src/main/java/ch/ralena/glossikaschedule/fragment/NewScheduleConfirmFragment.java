package ch.ralena.glossikaschedule.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import ch.ralena.glossikaschedule.NewScheduleActivity;
import ch.ralena.glossikaschedule.R;

public class NewScheduleConfirmFragment extends Fragment {
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		// load activity reference
		NewScheduleActivity activity = (NewScheduleActivity) getActivity();
		activity.getSupportActionBar().setTitle("Name your schedule");
		// load view
		View view = inflater.inflate(R.layout.fragment_new_schedule_confirm, container, false);
		Button button = view.findViewById(R.id.createScheduleButton);
		ImageView flagImage = view.findViewById(R.id.flagImageView);
		TextView languageName = view.findViewById(R.id.languageLabel);
		TextView scheduleName = view.findViewById(R.id.scheduleTitleLabel);
		EditText title = view.findViewById(R.id.scheduleTitle);
		// load values into views
		flagImage.setImageResource(activity.selectedLanguage.getDrawable());
		languageName.setText(activity.selectedLanguage.getName());
		scheduleName.setText(activity.selectedSchedule.getTitle());
		title.setText(activity.selectedSchedule.getTitle());
		// onclick for submitting schedule
		button.setOnClickListener(v -> activity.createSchedule(title.getText().toString()));
		// request focus for edittext
		title.requestFocus();
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		return view;
	}
}
