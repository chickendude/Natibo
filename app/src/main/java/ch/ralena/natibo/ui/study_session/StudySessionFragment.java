package ch.ralena.natibo.ui.study_session;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ch.ralena.natibo.ui.MainActivity;
import ch.ralena.natibo.R;
import ch.ralena.natibo.ui.adapter.SentenceGroupAdapter;
import ch.ralena.natibo.data.room.object.Course;
import ch.ralena.natibo.data.room.object.Day;
import ch.ralena.natibo.data.room.object.SentenceGroup;
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
	SentenceGroupAdapter adapter;

	// views
	private TextView remainingRepsText;
	private TextView remainingTimeText;
	private TextView totalRepsText;
	private ImageView playPauseImage;
	private LinearLayout sentencesLayout;

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

		// load all the views
		loadGlobalViews(view);

		// set up recycler view
		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		adapter = new SentenceGroupAdapter();
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		// handle playing/pausing
		playPauseImage.setOnClickListener(this::playPause);

		// connect to the studySessionService and start the session
		connectToService();
		activity.startSession(course);

		return view;
	}

	private void loadGlobalViews(View view) {
		// load views
		remainingRepsText = view.findViewById(R.id.remainingRepsText);
		remainingTimeText = view.findViewById(R.id.remainingTimeText);
		totalRepsText = view.findViewById(R.id.totalRepsText);
		playPauseImage = view.findViewById(R.id.playPauseImage);
		sentencesLayout = view.findViewById(R.id.sentencesLayout);

		// hide sentences layout until a sentence has been loaded
		sentencesLayout.setVisibility(View.VISIBLE);

		// load course title
		TextView courseTitleLabel = view.findViewById(R.id.courseTitleText);
		courseTitleLabel.setText(course.getTitle());

		// settings
		ImageView settingsIcon = view.findViewById(R.id.settingsIcon);
		settingsIcon.setOnClickListener(v -> Toast.makeText(activity, R.string.course_settings_not_implemented, Toast.LENGTH_SHORT).show());
	}

	private void connectToService() {
		serviceDisposable = activity.getSessionPublish().subscribe(service -> {
			studySessionService = service;
			if (course.getCurrentDay().getCurrentSentenceGroup() != null)
				nextSentence(course.getCurrentDay().getCurrentSentenceGroup());
			else
				sessionFinished(course.getCurrentDay());
			sentenceDisposable = studySessionService.sentenceObservable().subscribe(this::nextSentence);
			finishDisposable = studySessionService.finishObservable().subscribe(this::sessionFinished);
			setPaused(studySessionService.getPlaybackStatus() == null || studySessionService.getPlaybackStatus() == StudySessionService.PlaybackStatus.PAUSED);
			if (!isPaused) {
				startTimer();
			}
			updateTime();
			updatePlayPauseImage();
		});
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
			course.addReps(course.getCurrentDay().getTotalReviews());
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

	private void nextSentence(SentenceGroup sentenceGroup) {
		updatePlayPauseImage();
		sentencesLayout.setVisibility(View.VISIBLE);
		adapter.updateSentenceGroup(sentenceGroup);

		// update number of reps remaining
		remainingRepsText.setText(String.format(Locale.getDefault(), "%d", course.getCurrentDay().getNumReviewsLeft()));
		totalRepsText.setText(String.format(Locale.getDefault(), "%d", course.getTotalReps()));

		// update time left
		millisLeft = course.getCurrentDay().getTimeLeft();
	}

	private void startTimer() {
		millisLeft = millisLeft - millisLeft % 1000 - 1;
		updateTime();
		// Make sure no two active timers are displayed at the same time
		if (countDownTimer != null) {
			countDownTimer.cancel();
		}
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
	}

	private void updateTime() {
		int secondsLeft = (int) (millisLeft / 1000);
		remainingTimeText.setText(String.format(Locale.US, "%d:%02d", secondsLeft / 60, secondsLeft % 60));
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
