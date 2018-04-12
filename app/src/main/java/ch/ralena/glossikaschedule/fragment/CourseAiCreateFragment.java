package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.object.Language;
import ch.ralena.glossikaschedule.utils.Utils;
import io.realm.Realm;

public class CourseAiCreateFragment extends Fragment {
	private static final String TAG = CourseAiCreateFragment.class.getSimpleName();
	public static final String TAG_BASE_LANGUAGE = "tag_base_language";
	public static final String TAG_TARGET_LANGUAGE = "tag_target_language";

	private Realm realm;
	Language baseLanguage;
	Language targetLanguage;

	// Views
	EditText sentencesPerDayEdit;
	SeekBar sentencesPerDaySeek;
	EditText customScheduleEdit;
	RadioGroup reviewScheduleRadioGroup;
	RadioButton fourDayRadio;
	RadioButton fiveDayRadio;
	RadioButton customDayRadio;

	// text watchers
	TextWatcher sentencesPerDayTextWatcher = new TextWatcher() {
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
	TextWatcher customScheduleTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() > 0) {
				String[] numbers = s.toString().split("[*.,/ ]");
				boolean areAllNumbers = true;
				for (String number : numbers) {
					areAllNumbers = areAllNumbers && Utils.isNumeric(number);
				}
				if (areAllNumbers) {
					String pattern = TextUtils.join(" / ", numbers);
					customDayRadio.setText(pattern);
				}
			} else {
				customDayRadio.setText("? / ? / ?");
			}
		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	};

	// seek bar change listener
	SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			sentencesPerDayEdit.removeTextChangedListener(sentencesPerDayTextWatcher);
			sentencesPerDayEdit.setText("" + (progress + 1));
			sentencesPerDayEdit.setSelection(sentencesPerDayEdit.getText().length());
			sentencesPerDayEdit.addTextChangedListener(sentencesPerDayTextWatcher);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}
	};

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_course_ai_create, container, false);

		realm = Realm.getDefaultInstance();

		String baseId = getArguments().getString(TAG_BASE_LANGUAGE);
		String targetId = getArguments().getString(TAG_TARGET_LANGUAGE);

		baseLanguage = realm.where(Language.class).equalTo("languageId", baseId).findFirst();
		targetLanguage = realm.where(Language.class).equalTo("languageId", targetId).findFirst();

		// get views
		TextView baseLanguageLabel = view.findViewById(R.id.baseLanguageLabel);
		TextView targetLanguageLabel = view.findViewById(R.id.targetLanguageLabel);
		sentencesPerDayEdit = view.findViewById(R.id.sentencesPerDayEdit);
		sentencesPerDaySeek = view.findViewById(R.id.sentencesPerDaySeek);
		customScheduleEdit = view.findViewById(R.id.customScheduleEdit);
		reviewScheduleRadioGroup = view.findViewById(R.id.reviewScheduleRadioGroup);
		fourDayRadio = view.findViewById(R.id.fourDayRadio);
		fiveDayRadio = view.findViewById(R.id.fiveDayRadio);
		customDayRadio = view.findViewById(R.id.customDayRadio);

		// display base and target language
		baseLanguageLabel.setText(baseLanguage.getLongName());
		targetLanguageLabel.setText(targetLanguage.getLongName());

		// custom schedule edit should start off gone
		customScheduleEdit.setVisibility(View.GONE);
		customScheduleEdit.addTextChangedListener(customScheduleTextWatcher);

		// set up sentencesPerDay EditText and SeekBar
		sentencesPerDayEdit.addTextChangedListener(sentencesPerDayTextWatcher);
		sentencesPerDaySeek.setOnSeekBarChangeListener(seekBarChangeListener);
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
