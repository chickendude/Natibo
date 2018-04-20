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

		Button startSessionButton = view.findViewById(R.id.startSessionButton);
		startSessionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (course.getTargetPacks().size() == 0) {
					Toast.makeText(getContext(), "Please add a book to your course first by clicking on it!", Toast.LENGTH_SHORT).show();
					return;
				}
				StudySessionFragment fragment = new StudySessionFragment();
				Bundle bundle = new Bundle();
				bundle.putString(StudySessionFragment.TAG_COURSE_ID, course.getId());
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
				course.getBasePacks().remove(pack);
				course.getBasePacks().remove(pack);
				course.getBasePacks().remove(pack);
				course.getBasePacks().remove(basePack);
				course.getBasePacks().remove(basePack);
				course.getBasePacks().remove(basePack);
				course.getTargetPacks().remove(pack);
				course.getTargetPacks().remove(pack);
				course.getTargetPacks().remove(pack);
				course.getTargetPacks().remove(basePack);
				course.getTargetPacks().remove(basePack);
				course.getTargetPacks().remove(basePack);
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
