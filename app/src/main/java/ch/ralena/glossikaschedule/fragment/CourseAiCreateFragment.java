package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import ch.ralena.glossikaschedule.R;
import io.realm.Realm;

public class CourseAiCreateFragment extends Fragment {
	private static final String TAG = CourseAiCreateFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "language_id";

	private Realm realm;

	// Views
	EditText customScheduleEdit;
	RadioGroup reviewScheduleRadioGroup;
	RadioButton fourDayRadio;
	RadioButton fiveDayRadio;
	RadioButton customDayRadio;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_course_ai_create, container, false);

		realm = Realm.getDefaultInstance();

		// get views
		customScheduleEdit = view.findViewById(R.id.customScheduleEdit);
		reviewScheduleRadioGroup = view.findViewById(R.id.reviewScheduleRadioGroup);
		fourDayRadio = view.findViewById(R.id.fourDayRadio);
		fiveDayRadio = view.findViewById(R.id.fiveDayRadio);
		customDayRadio = view.findViewById(R.id.customDayRadio);

		// custom schedule edit should start off gone
		customScheduleEdit.setVisibility(View.GONE);

		// set up radio listeners
		reviewScheduleRadioGroup.clearCheck();
		reviewScheduleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
			switch (checkedId) {
				case R.id.customDayRadio:
					customScheduleEdit.setVisibility(View.VISIBLE);
					break;
				default:
					customScheduleEdit.setVisibility(View.GONE);
					break;
			}
		});

		return view;
	}
}
