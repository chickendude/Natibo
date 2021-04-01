package ch.ralena.natibo.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.UUID;

import ch.ralena.natibo.ui.MainActivity;
import ch.ralena.natibo.R;
import ch.ralena.natibo.data.room.object.Course;
import ch.ralena.natibo.data.room.object.Language;
import ch.ralena.natibo.data.room.object.Schedule;
import ch.ralena.natibo.utils.Utils;
import io.realm.Realm;
import io.realm.RealmList;

// TODO: 13/04/18 move to course detail page
public class CoursePreparationFragment extends Fragment {
	private static final String TAG = CoursePreparationFragment.class.getSimpleName();
	public static final String TAG_LANGUAGE_IDS = "tag_language_ids";

	private Realm realm;
	RealmList<Language> languages;

	// Views
	EditText languageNamesLabel;
	EditText sentencesPerDayEdit;
	SeekBar sentencesPerDaySeek;
	EditText customScheduleEdit;
	RadioGroup reviewScheduleRadioGroup;
	RadioButton fourDayRadio;
	RadioButton fiveDayRadio;
	RadioButton customDayRadio;
	CheckBox chorusCheckBox;

	MainActivity activity;

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
		View view = inflater.inflate(R.layout.fragment_course_preparation, container, false);

		// switch to back button
		activity = (MainActivity) getActivity();
		activity.enableBackButton();
		activity.setTitle("Create an AI Course");

		// we have a check button
		setHasOptionsMenu(true);

		realm = Realm.getDefaultInstance();

		// load arguments
		String[] languageIds = getArguments().getStringArray(TAG_LANGUAGE_IDS);

		// load languages passed in from create course fragment
		languages = new RealmList<>();
		for (String languageId : languageIds) {
			languages.add(realm.where(Language.class).equalTo("languageId", languageId).findFirst());
		}

		// get views
		languageNamesLabel = view.findViewById(R.id.languageNamesLabel);
		sentencesPerDayEdit = view.findViewById(R.id.sentencesPerDayEdit);
		sentencesPerDaySeek = view.findViewById(R.id.sentencesPerDaySeek);
		customScheduleEdit = view.findViewById(R.id.customScheduleEdit);
		reviewScheduleRadioGroup = view.findViewById(R.id.reviewScheduleRadioGroup);
		fourDayRadio = view.findViewById(R.id.fourDayRadio);
		fiveDayRadio = view.findViewById(R.id.fiveDayRadio);
		customDayRadio = view.findViewById(R.id.customDayRadio);
		chorusCheckBox = view.findViewById(R.id.chorusCheckBox);

		// display languages in the course
		ArrayList<String> languageNames = new ArrayList<>();
		for (Language language : languages) {
			languageNames.add(language.getLongName());
		}
		languageNamesLabel.setText(TextUtils.join(" → ", languageNames));
		languageNamesLabel.setOnClickListener(v -> {
			languageNamesLabel.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		});

		// custom schedule edit should start off gone
		customScheduleEdit.setVisibility(View.GONE);
		customScheduleEdit.addTextChangedListener(customScheduleTextWatcher);

		// set up sentencesPerDay EditText and SeekBar
		sentencesPerDayEdit.addTextChangedListener(sentencesPerDayTextWatcher);
		sentencesPerDaySeek.setOnSeekBarChangeListener(seekBarChangeListener);
		sentencesPerDaySeek.setProgress(9);

		// set up radio listeners
		reviewScheduleRadioGroup.check(R.id.fourDayRadio);
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.check_toolbar, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_confirm:
				String id = createCourse();
				activity.loadCourseListFragment(id);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private String createCourse() {
		int checkedRadioId = reviewScheduleRadioGroup.getCheckedRadioButtonId();
		RadioButton checkedButton = getActivity().findViewById(checkedRadioId);
		String[] dailyReviews = checkedButton.getText().toString().split(" / ");
		int numSentencesPerDay = Integer.parseInt(sentencesPerDayEdit.getText().toString());

		// "base-target-target" if chorus enabled, otherwise "base-target"
		StringBuilder order = new StringBuilder();

		for (int i = 0; i < languages.size(); i++) {
			if ((i > 0 || languages.size() == 1) && chorusCheckBox.isChecked())
				order.append(i);
			order.append(i);
		}

		// --- begin transaction
		realm.beginTransaction();
		// create sentence schedule
		Schedule schedule = realm.createObject(Schedule.class, UUID.randomUUID().toString());
		schedule.setOrder(order.toString());
		schedule.setNumSentences(numSentencesPerDay);
		for (String review : dailyReviews) {
			schedule.getReviewPattern().add(Integer.parseInt(review));
		}

		// build course
		Course course = realm.createObject(Course.class, UUID.randomUUID().toString());

		course.setTitle(languageNamesLabel.getText().toString());
		course.setLanguages(languages);
		course.setPauseMillis(1000);
		course.setSchedule(schedule);
		realm.commitTransaction();
		// --- end transaction
		return course.getId();
	}
}
