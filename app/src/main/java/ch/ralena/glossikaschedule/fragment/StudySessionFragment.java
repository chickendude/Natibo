package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.ralena.glossikaschedule.MainActivity;
import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.adapter.StudySessionAdapter;
import ch.ralena.glossikaschedule.object.Course;
import ch.ralena.glossikaschedule.object.Day;
import ch.ralena.glossikaschedule.object.SentenceSet;
import ch.ralena.glossikaschedule.service.StudySessionService;
import io.realm.Realm;

public class StudySessionFragment extends Fragment {
	private static final String TAG = StudySessionFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "language_id";

	Course course;

	private Realm realm;

	private MainActivity activity;

	private StudySessionService studySessionService;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_study_session, container, false);

		activity = (MainActivity) getActivity();

		// load schedules from database
		String id = getArguments().getString(TAG_COURSE_ID);
		realm = Realm.getDefaultInstance();
		course = realm.where(Course.class).equalTo("id", id).findFirst();

		// load language name
		TextView courseTitleLabel = view.findViewById(R.id.courseTitleLabel);
		courseTitleLabel.setText(course.getTitle());

		Day day = course.getNextDay(realm);
		SentenceSet sentenceSet = day.getSentenceSets().get(0);

		// set up recyclerlist and adapter
		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		StudySessionAdapter adapter = new StudySessionAdapter(course.getTargetLanguage().getLanguageId(), sentenceSet.getSentences());
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(layoutManager);

//		adapter.asObservable().subscribe(this::loadSentenceListFragment);

		activity.getSessionPublish().subscribe(service -> studySessionService = service);

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (studySessionService != null) {
			studySessionService.removeNotification();
			studySessionService.stopSelf();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		activity.startSession(course.getCurrentDay());
	}
}
