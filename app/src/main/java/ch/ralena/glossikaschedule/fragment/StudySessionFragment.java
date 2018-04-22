package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import ch.ralena.glossikaschedule.MainActivity;
import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.object.Course;
import ch.ralena.glossikaschedule.object.Day;
import ch.ralena.glossikaschedule.object.Sentence;
import ch.ralena.glossikaschedule.object.SentencePair;
import ch.ralena.glossikaschedule.service.StudySessionService;
import io.realm.Realm;

public class StudySessionFragment extends Fragment {
	private static final String TAG = StudySessionFragment.class.getSimpleName();
	public static final String KEY_COURSE_ID = "language_id";

	Course course;

	private Realm realm;

	private MainActivity activity;

	private StudySessionService studySessionService;
	private long millisLeft;
	CountDownTimer countDownTimer;

	// views
	private TextView remainingRepsText;
	private TextView remainingTimeText;
	private ImageView playPauseImage;

	// base language views
	private TextView baseLanguageCodeText;
	private TextView baseSentenceText;
	private TextView baseAlternateSentenceText;
	private LinearLayout baseAlternateSentenceLayout;
	private TextView baseRomanizationText;
	private LinearLayout baseRomanizationLayout;
	private TextView baseIpaText;
	private LinearLayout baseIpaLayout;

	// target language views
	private TextView targetLanguageCodeText;
	private TextView targetSentenceText;
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
		String id = getArguments().getString(KEY_COURSE_ID);
		realm = Realm.getDefaultInstance();
		course = realm.where(Course.class).equalTo("id", id).findFirst();

		// if the current day is done, start the next one
		if (course.getCurrentDay() == null || course.getCurrentDay().isCompleted())
			course.prepareNextDay(realm);

		// load views
		remainingRepsText = view.findViewById(R.id.remainingRepsText);
		remainingTimeText = view.findViewById(R.id.remainingTimeText);
		playPauseImage = view.findViewById(R.id.playPauseImage);

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

		// handle playing/pausing
		playPauseImage.setOnClickListener(this::playPause);

		activity.getSessionPublish().subscribe(service -> {
			nextSentence(course.getCurrentDay().getCurrentSentencePair());
			studySessionService = service;
			studySessionService.sentenceObservable().subscribe(this::nextSentence);
			studySessionService.finishObservable().subscribe(this::sessionFinished);
			updatePlayPauseImage();
		});

		return view;
	}

	private void playPause(View view) {
		if (studySessionService != null) {
			if (studySessionService.getPlaybackStatus() == StudySessionService.PlaybackStatus.PLAYING) {
				studySessionService.pause();
			} else {
				studySessionService.resume();
			}
			updatePlayPauseImage();
		}
	}

	private void updatePlayPauseImage() {
		if (studySessionService.getPlaybackStatus() == StudySessionService.PlaybackStatus.PLAYING) {
			playPauseImage.setImageResource(R.drawable.ic_pause);
		} else {
			playPauseImage.setImageResource(R.drawable.ic_play);
		}
	}

	private void sessionFinished(Day day) {
		// mark day as completed
		realm.executeTransaction(r-> {
			course.addReps(course.getCurrentDay().getNumReps());
			day.setCompleted(true);
		});

		StudySessionOverviewFragment fragment = new StudySessionOverviewFragment();
		Bundle bundle = new Bundle();
		bundle.putString(StudySessionOverviewFragment.KEY_COURSE_ID, course.getId());
		fragment.setArguments(bundle);

		getFragmentManager().beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.commit();
	}

	private void nextSentence(SentencePair sentencePair) {
		Sentence baseSentence = sentencePair.getBaseSentence();
		Sentence targetSentence = sentencePair.getTargetSentence();

		// update number of reps remaining
		remainingRepsText.setText(String.format(Locale.getDefault(), "%d", course.getCurrentDay().getNumReviewsLeft()));

		// update countdown timer
		millisLeft = course.getCurrentDay().getTimeLeft();
		if (countDownTimer != null)
			countDownTimer.cancel();

		// there
		Handler handler = new Handler();
		handler.postDelayed(() -> {
			millisLeft = millisLeft - millisLeft % 1000 - 1;
			updateTime();
			countDownTimer = new CountDownTimer(millisLeft, 1000) {
				@Override
				public void onTick(long millisUntilFinished) {
					millisLeft = millisUntilFinished;
					updateTime();
				}

				@Override
				public void onFinish() {

				}
			}.start();
		}, millisLeft % 1000);
		updateTime();

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

	private void updateTime() {
		int secondsLeft = (int) (millisLeft / 1000);
		remainingTimeText.setText(String.format(Locale.US, "%d:%02d", secondsLeft / 60, secondsLeft % 60));
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (countDownTimer != null)
			countDownTimer.cancel();
	}
}