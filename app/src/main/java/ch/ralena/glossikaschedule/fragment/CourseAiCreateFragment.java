package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.utils.Utils;
import io.realm.Realm;

public class CourseAiCreateFragment extends Fragment {
	private static final String TAG = CourseAiCreateFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "language_id";

	private Realm realm;

	// Views
	EditText sentencesPerDayEdit;
	SeekBar sentencesPerDaySeek;
	EditText customScheduleEdit;
	RadioGroup reviewScheduleRadioGroup;
	RadioButton fourDayRadio;
	RadioButton fiveDayRadio;
	RadioButton customDayRadio;

	TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (Utils.isNumeric(s.toString())) {
				int number = Integer.parseInt(s.toString());
				if (number <= 100) {
					sentencesPerDaySeek.setProgress(number - 1);
					sentencesPerDayEdit.setSelection(start + count);
				} else {
					sentencesPerDayEdit.setText("" + 100);
				}
			}
		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	};

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_course_ai_create, container, false);

		realm = Realm.getDefaultInstance();

		// get views
		sentencesPerDayEdit = view.findViewById(R.id.sentencesPerDayEdit);
		sentencesPerDaySeek = view.findViewById(R.id.sentencesPerDaySeek);
		customScheduleEdit = view.findViewById(R.id.customScheduleEdit);
		reviewScheduleRadioGroup = view.findViewById(R.id.reviewScheduleRadioGroup);
		fourDayRadio = view.findViewById(R.id.fourDayRadio);
		fiveDayRadio = view.findViewById(R.id.fiveDayRadio);
		customDayRadio = view.findViewById(R.id.customDayRadio);

		// custom schedule edit should start off gone
		customScheduleEdit.setVisibility(View.GONE);

		// set up sentencesPerDay EditText and SeekBar
		String textBefore;
		String textAfter;
		sentencesPerDayEdit.addTextChangedListener(textWatcher);
		sentencesPerDaySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				sentencesPerDayEdit.removeTextChangedListener(textWatcher);
				sentencesPerDayEdit.setText("" + (progress + 1));
				sentencesPerDayEdit.setSelection(sentencesPerDayEdit.getText().length());
				sentencesPerDayEdit.addTextChangedListener(textWatcher);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
		sentencesPerDaySeek.setProgress(9);

		// set up radio listeners
		reviewScheduleRadioGroup.clearCheck();
		reviewScheduleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
			switch (checkedId) {
				case R.id.customDayRadio:
					customScheduleEdit.setVisibility(View.VISIBLE);
					customScheduleEdit.requestFocus();
					break;
				default:
					customScheduleEdit.setVisibility(View.GONE);
					break;
			}
		});

		return view;
	}
}
