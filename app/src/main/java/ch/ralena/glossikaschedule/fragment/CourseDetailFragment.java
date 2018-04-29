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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.adapter.CourseDetailAdapter;
import ch.ralena.glossikaschedule.object.Course;
import ch.ralena.glossikaschedule.object.Language;
import ch.ralena.glossikaschedule.object.Pack;
import io.realm.Realm;
import io.realm.RealmList;

// TODO: 13/04/18 if no sentence sets have been chosen, prompt to select sentence packs.
public class CourseDetailFragment extends Fragment {
	private static final String TAG = CourseDetailFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "language_id";

	Course course;

	private Realm realm;

	CourseDetailAdapter adapter;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_course_detail, container, false);

		// load schedules from database
		String id = getArguments().getString(TAG_COURSE_ID);
		realm = Realm.getDefaultInstance();
		course = realm.where(Course.class).equalTo("id", id).findFirst();

		getActivity().setTitle(course.getTitle());

		// load total reps
		TextView totalRepsText = view.findViewById(R.id.totalRepsText);
		totalRepsText.setText(String.format(Locale.US, "%d", course.getNumReps()));

		// load total sentences seen
		TextView totalSentencesSeenText = view.findViewById(R.id.totalSentencesSeenText);
		totalSentencesSeenText.setText(String.format(Locale.US, "%d", course.getNumSentencesSeen()));

		// load flag image
		ImageView flagImage = view.findViewById(R.id.flagImageView);
		flagImage.setImageResource(course.getTargetLanguage().getLanguageType().getDrawable());

		// load language name
		Language targetLanguage = course.getTargetLanguage();
		TextView languageLabel = view.findViewById(R.id.languageLabel);
		languageLabel.setText(targetLanguage.getLanguageType().getName());

		RealmList<Pack> matchingPacks = targetLanguage.getMatchingPacks(course.getBaseLanguage());

		// set up recyclerlist and adapter
		RecyclerView recyclerView = view.findViewById(R.id.booksRecyclerView);
		adapter = new CourseDetailAdapter(course.getTargetPacks(), matchingPacks);
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		recyclerView.setLayoutManager(layoutManager);

		adapter.asObservable().subscribe(this::addRemovePack);

		// set up button
		Button startSessionButton = view.findViewById(R.id.startSessionButton);
		startSessionButton.setText(
				course.getCurrentDay().isCompleted() ? R.string.start_session : R.string.continue_session
		);
		startSessionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// make sure we have books added before starting, otherwise it'll crash!
				if (course.getTargetPacks().size() == 0) {
					Toast.makeText(getContext(), "Please add a book to your course first by clicking on it!", Toast.LENGTH_SHORT).show();
					return;
				}

				// now we can load the fragment
				StudySessionFragment fragment = new StudySessionFragment();
				Bundle bundle = new Bundle();
				bundle.putString(StudySessionFragment.KEY_COURSE_ID, course.getId());
				fragment.setArguments(bundle);

				getFragmentManager().beginTransaction()
						.replace(R.id.fragmentPlaceHolder, fragment)
						.addToBackStack(null)
						.commit();
			}
		});

		return view;
	}

	private void addRemovePack(Pack pack) {
		Pack basePack = course.getBaseLanguage().getMatchingPack(pack);
		if (course.getTargetPacks().contains(pack)) {
			realm.executeTransaction(r -> {
				course.getBasePacks().remove(basePack);
				course.getTargetPacks().remove(pack);
			});
		} else {
			realm.executeTransaction(r -> {
				course.getBasePacks().add(basePack);
				course.getTargetPacks().add(pack);
			});
		}
		adapter.notifyDataSetChanged();
	}
}
