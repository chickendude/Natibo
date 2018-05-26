package ch.ralena.natibo.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ch.ralena.natibo.MainActivity;
import ch.ralena.natibo.R;
import ch.ralena.natibo.object.Course;
import ch.ralena.natibo.object.Day;
import ch.ralena.natibo.object.Sentence;
import ch.ralena.natibo.object.SentencePair;
import ch.ralena.natibo.service.StudySessionService;
import io.reactivex.disposables.Disposable;
import io.realm.Realm;

public class StudySessionFragment extends Fragment {
	private static final String TAG = StudySessionFragment.class.getSimpleName();
	public static final String KEY_COURSE_ID = "language_id";
	private static final String KEY_IS_PAUSED = "key_is_paused";

	Course course;

	private Realm realm;

	private MainActivity activity;

	// fields
	private SharedPreferences prefs;
	private StudySessionService studySessionService;
	private long millisLeft;
	private CountDownTimer countDownTimer;
	private boolean isPaused;

	// views
	private TextView remainingRepsText;
	private TextView remainingTimeText;
	private TextView totalRepsText;
	private ImageView playPauseImage;
	private LinearLayout sentencesLayout;

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

	Disposable serviceDisposable;
	Disposable sentenceDisposable;
	Disposable finishDisposable;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_study_session, container, false);

		activity = (MainActivity) getActivity();

		if (savedInstanceState != null) {
			isPaused = savedInstanceState.getBoolean(KEY_IS_PAUSED, true);
		} else {
			isPaused = true;
		}

		// load schedules from database
		String id = getArguments().getString(KEY_COURSE_ID);
		realm = Realm.getDefaultInstance();
		course = realm.where(Course.class).equalTo("id", id).findFirst();

		// if the current day is done, start the next one
		if (course.getCurrentDay() == null || course.getCurrentDay().isCompleted())
			course.prepareNextDay(realm);

		// in a separate method since we have so many views!
		loadGlobalViews(view);

		baseLanguageCodeText.setText(course.getBaseLanguage().getLanguageId());
		targetLanguageCodeText.setText(course.getTargetLanguage().getLanguageId());

		// load language name
		TextView courseTitleLabel = view.findViewById(R.id.courseTitleText);
		courseTitleLabel.setText(course.getTitle());

		// hide sentences layout until a sentence has been loaded
		sentencesLayout.setVisibility(View.INVISIBLE);

		// handle playing/pausing
		playPauseImage.setOnClickListener(this::playPause);

		return view;
	}

	private void loadGlobalViews(View view) {
		// load views
		remainingRepsText = view.findViewById(R.id.remainingRepsText);
		remainingTimeText = view.findViewById(R.id.remainingTimeText);
		totalRepsText = view.findViewById(R.id.totalRepsText);
		playPauseImage = view.findViewById(R.id.playPauseImage);
		sentencesLayout = view.findViewById(R.id.sentencesLayout);

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

		// settings
		ImageView settingsIcon = view.findViewById(R.id.settingsIcon);
		settingsIcon.setOnClickListener(v -> Toast.makeText(activity, R.string.course_settings_not_implemented, Toast.LENGTH_SHORT).show());
	}

	private void connectToService() {
		serviceDisposable = activity.getSessionPublish().subscribe(service -> {
			if (course.getCurrentDay().getCurrentSentencePair() != null)
				nextSentence(course.getCurrentDay().getCurrentSentencePair());
			else
				sessionFinished(course.getCurrentDay());
			studySessionService = service;
			sentenceDisposable = studySessionService.sentenceObservable().subscribe(this::nextSentence);
			finishDisposable = studySessionService.finishObservable().subscribe(this::sessionFinished);
			setPaused(studySessionService.getPlaybackStatus() == null || studySessionService.getPlaybackStatus() == StudySessionService.PlaybackStatus.PAUSED);
			if (!isPaused) {
				startTimer();
			}
			updateTime();
			updatePlayPauseImage();
		});
		activity.startSession(course);
	}

	private void playPause(View view) {
		if (studySessionService != null) {
			if (studySessionService.getPlaybackStatus() == StudySessionService.PlaybackStatus.PLAYING) {
				studySessionService.pause();
				setPaused(true);
				if (countDownTimer != null)
					countDownTimer.cancel();
			} else {
				studySessionService.resume();
				startTimer();
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
		realm.executeTransaction(r -> {
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
		sentencesLayout.setVisibility(View.VISIBLE);

		Sentence baseSentence = sentencePair.getBaseSentence();
		Sentence targetSentence = sentencePair.getTargetSentence();

		// update number of reps remaining
		remainingRepsText.setText(String.format(Locale.getDefault(), "%d", course.getCurrentDay().getNumReviewsLeft()));
		totalRepsText.setText(String.format(Locale.getDefault(), "%d", course.getTotalReps()));

		// update time left
		millisLeft = course.getCurrentDay().getTimeLeft();

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

	private void startTimer() {
		millisLeft = millisLeft - millisLeft % 1000 - 1;
		updateTime();
		countDownTimer = new CountDownTimer(millisLeft, 100) {
			@Override
			public void onTick(long millisUntilFinished) {
				if (!isPaused) {
					millisLeft = millisUntilFinished;
					updateTime();
				}
			}

			@Override
			public void onFinish() {
				Log.d(TAG, "timer finished: " + this.toString());
			}
		};
		setPaused(false);
		countDownTimer.start();
	}

	private void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
//		prefs.edit().putBoolean(KEY_IS_PAUSED, isPaused).apply();
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
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_IS_PAUSED, isPaused);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (serviceDisposable != null)
			serviceDisposable.dispose();
		if (sentenceDisposable != null)
			sentenceDisposable.dispose();
		if (finishDisposable != null)
			finishDisposable.dispose();
		if (countDownTimer != null)
			countDownTimer.cancel();
	}

	@Override
	public void onResume() {
		super.onResume();
		connectToService();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (countDownTimer != null)
			countDownTimer.cancel();
	}
}
