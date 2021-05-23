package ch.ralena.natibo.ui.study.overview;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.ralena.natibo.ui.MainActivity;
import ch.ralena.natibo.R;
import ch.ralena.natibo.ui.study.insession.adapter.StudySessionAdapter;
import ch.ralena.natibo.data.room.object.Course;
import ch.ralena.natibo.data.room.object.Day;
import ch.ralena.natibo.data.room.object.SentenceGroup;
import ch.ralena.natibo.data.room.object.SentenceSet;
import io.realm.Realm;

public class StudySessionOverviewFragment extends Fragment {
	private static final String TAG = StudySessionOverviewFragment.class.getSimpleName();
	public static final String KEY_COURSE_ID = "language_id";

	Course course;

	private Realm realm;

	private MainActivity activity;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_study_session_overview, container, false);

		activity = (MainActivity) getActivity();
		activity.setTitle(getString(R.string.session_overview));

		// load schedules from database
		String id = getArguments().getString(KEY_COURSE_ID);
		realm = Realm.getDefaultInstance();
		course = realm.where(Course.class).equalTo("id", id).findFirst();

		// load language name
		TextView courseTitleLabel = view.findViewById(R.id.courseTitleText);
		courseTitleLabel.setText(course.getTitle());

		Day day = course.getCurrentDay();
		List<SentenceGroup> sentenceGroups = new ArrayList<>();
		for (SentenceSet sentenceSet : day.getSentenceSets()) {
			sentenceGroups.addAll(sentenceSet.getSentenceSet());
		}

		// set up recyclerlist and adapter
		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		StudySessionAdapter adapter = new StudySessionAdapter(course.getLanguages(), sentenceGroups);
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(layoutManager);

//		adapter.asObservable().subscribe(this::loadSentenceListFragment);

		return view;
	}


}
