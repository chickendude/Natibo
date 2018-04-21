package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ch.ralena.glossikaschedule.MainActivity;
import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.object.Course;
import ch.ralena.glossikaschedule.object.Sentence;
import ch.ralena.glossikaschedule.object.SentencePair;
import ch.ralena.glossikaschedule.service.StudySessionService;
import io.realm.Realm;

public class StudySessionFragment extends Fragment {
	private static final String TAG = StudySessionFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "language_id";

	Course course;

	private Realm realm;

	private MainActivity activity;

	private StudySessionService studySessionService;

	// base language views
	TextView baseLanguageCodeText;
	TextView baseSentenceText;
	private TextView baseAlternateSentenceText;
	private LinearLayout baseAlternateSentenceLayout;
	private TextView baseRomanizationText;
	private LinearLayout baseRomanizationLayout;
	private TextView baseIpaText;
	private LinearLayout baseIpaLayout;

	// target language views
	TextView targetLanguageCodeText;
	TextView targetSentenceText;
	private TextView targetAlternateSentenceText;
	private LinearLayout targetAlternateSentenceLayout;
	private TextView targetRomanizationText;
	private LinearLayout targetRomanizationLayout;
	private TextView targetIpaText;
	private LinearLayout targetIpaLayout;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_study_session, container, false);

		activity = (MainActivity) getActivity();

		// load schedules from database
		String id = getArguments().getString(TAG_COURSE_ID);
		realm = Realm.getDefaultInstance();
		course = realm.where(Course.class).equalTo("id", id).findFirst();

		// load base language views
		baseLanguageCodeText = view.findViewById(R.id.baseLanguageCodeText);
		baseSentenceText = view.findViewById(R.id.baseSentenceText);
		baseAlternateSentenceText = view.findViewById(R.id.baseAlternateSentenceText);
		baseAlternateSentenceLayout = view.findViewById(R.id.baseAlternateSentenceLayout);
		baseRomanizationText = view.findViewById(R.id.baseRomanizationText);
		baseRomanizationLayout = view.findViewById(R.id.baseRomanizationLayout);
		baseIpaText = view.findViewById(R.id.baseIpaText);
		baseIpaLayout = view.findViewById(R.id.baseIpaLayout);

		// load target language views
		targetLanguageCodeText = view.findViewById(R.id.targetLanguageCodeText);
		targetSentenceText = view.findViewById(R.id.targetSentenceText);
		targetAlternateSentenceText = view.findViewById(R.id.targetAlternateSentenceText);
		targetAlternateSentenceLayout = view.findViewById(R.id.targetAlternateSentenceLayout);
		targetRomanizationText = view.findViewById(R.id.targetRomanizationText);
		targetRomanizationLayout = view.findViewById(R.id.targetRomanizationLayout);
		targetIpaText = view.findViewById(R.id.targetIpaText);
		targetIpaLayout = view.findViewById(R.id.targetIpaLayout);

		baseLanguageCodeText.setText(course.getBaseLanguage().getLanguageId());
		targetLanguageCodeText.setText(course.getTargetLanguage().getLanguageId());

		// load language name
		TextView courseTitleLabel = view.findViewById(R.id.courseTitleText);
		courseTitleLabel.setText(course.getTitle());

		activity.getSessionPublish().subscribe(service -> {
			studySessionService = service;
			studySessionService.sentenceObservable().subscribe(this::nextSentence);
		});

		return view;
	}

	private void nextSentence(SentencePair sentencePair) {
		Sentence baseSentence = sentencePair.getBaseSentence();
		Sentence targetSentence = sentencePair.getTargetSentence();
		// update base sentence views
		baseSentenceText.setText(baseSentence.getText());
		updateSentencePart(baseAlternateSentenceLayout, baseAlternateSentenceText, baseSentence.getAlternate());
		updateSentencePart(baseRomanizationLayout, baseRomanizationText, baseSentence.getRomanization());
		updateSentencePart(baseIpaLayout, baseIpaText, baseSentence.getIpa());
		// update target sentence views
		targetSentenceText.setText(targetSentence.getText());
		updateSentencePart(targetAlternateSentenceLayout, targetAlternateSentenceText, targetSentence.getAlternate());
		updateSentencePart(targetRomanizationLayout, targetRomanizationText, targetSentence.getRomanization());
		updateSentencePart(targetIpaLayout, targetIpaText, targetSentence.getIpa());
	}

	private void updateSentencePart(ViewGroup layout, TextView textView, String text) {
		if (text != null) {
			layout.setVisibility(View.VISIBLE);
			textView.setText(text);
		} else {
			layout.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		activity.startSession(course.getCurrentDay());
	}
}
