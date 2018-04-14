package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.object.Course;
import io.realm.Realm;

// TODO: 13/04/18 if no sentence sets have been chosen, prompt to select sentence packs.
public class CourseDetailFragment extends Fragment {
	private static final String TAG = CourseDetailFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "language_id";

	Course course;

	private Realm realm;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_language_detail, container, false);

		// load schedules from database
		String id = getArguments().getString(TAG_COURSE_ID);
		realm = Realm.getDefaultInstance();
		course = realm.where(Course.class).equalTo("id", id).findFirst();

		// load flag image
		ImageView flagImage = view.findViewById(R.id.flagImageView);
		flagImage.setImageResource(course.getTargetLanguage().getLanguageType().getDrawable());

		// load language name
		TextView languageLabel = view.findViewById(R.id.languageLabel);
		languageLabel.setText(course.getTargetLanguage().getLanguageType().getName());

		// set up recyclerlist and adapter
//		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
//		LanguageDetailAdapter adapter = new LanguageDetailAdapter(getContext(), packs);
//		recyclerView.setAdapter(adapter);
//		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
//		recyclerView.setLayoutManager(layoutManager);
//
//		adapter.asObservable().subscribe(this::loadSentenceListFragment);

		return view;
	}
}
