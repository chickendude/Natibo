package ch.ralena.natibo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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

import ch.ralena.natibo.MainActivity;
import ch.ralena.natibo.R;
import ch.ralena.natibo.adapter.CourseDetailAdapter;
import ch.ralena.natibo.object.Course;
import ch.ralena.natibo.object.Language;
import ch.ralena.natibo.object.Pack;
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
		Language targetLanguage = course.getLanguages().first();

		MainActivity activity = (MainActivity) getActivity();
		activity.setTitle(course.getTitle());
		activity.enableHomeButton();

		loadCourseInfo(view, targetLanguage);

		RealmList<Pack> matchingPacks = targetLanguage.getMatchingPacks(course.getLanguages().last());

		// set up recyclerlist and adapter
		RecyclerView recyclerView = view.findViewById(R.id.booksRecyclerView);
		adapter = new CourseDetailAdapter(course.getPacks(), matchingPacks);
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		recyclerView.setLayoutManager(layoutManager);

		adapter.asObservable().subscribe(this::addRemovePack);

		// set up icons and button
		prepareDeleteCourseIcon(view, activity);
		prepareSettingsIcon(view, activity);
		prepareStartSessionButton(view);

		return view;
	}

	private void prepareStartSessionButton(View view) {
		Button startSessionButton = view.findViewById(R.id.startSessionButton);

		startSessionButton.setText(
				course.getCurrentDay() == null || course.getCurrentDay().isCompleted() ? R.string.start_session : R.string.continue_session
		);
		startSessionButton.setOnClickListener(v -> {
			// make sure we have books added before starting, otherwise it'll crash!
			if (course.getPacks().size() == 0) {
				Toast.makeText(getContext(), R.string.add_book_first, Toast.LENGTH_SHORT).show();
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
		});
	}

	private void loadCourseInfo(View view, Language targetLanguage) {
		// load total reps
		TextView totalRepsText = view.findViewById(R.id.totalRepsText);
		totalRepsText.setText(String.format(Locale.US, "%d", course.getTotalReps()));

		// load total sentences seen
		TextView totalSentencesSeenText = view.findViewById(R.id.totalSentencesSeenText);
		totalSentencesSeenText.setText(String.format(Locale.US, "%d", course.getNumSentencesSeen()));

		// load flag image
		ImageView flagImage = view.findViewById(R.id.flagImageView);
		flagImage.setImageResource(course.getLanguages().last().getLanguageType().getDrawable());

		// load language name
		TextView languageLabel = view.findViewById(R.id.languageLabel);
		languageLabel.setText(targetLanguage.getLanguageType().getName());
	}

	private void prepareDeleteCourseIcon(View view, MainActivity activity) {
		ImageView deleteIcon = view.findViewById(R.id.deleteIcon);
		View.OnClickListener deleteConfirmListener =
				v -> {
					realm.executeTransaction(realm -> realm.where(Course.class).equalTo("id", course.getId()).findFirst().deleteFromRealm());
					activity.stopSession();
					activity.loadCourseListFragment();
				};


		deleteIcon.setOnClickListener(v -> {
			Snackbar.make(view, R.string.confirm_delete, Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.delete, deleteConfirmListener)
					.show();
		});
	}

	private void prepareSettingsIcon(View view, MainActivity activity) {
		ImageView settingsIcon = view.findViewById(R.id.settingsIcon);
		settingsIcon.setOnClickListener(v -> {
			CourseSettingsFragment fragment = new CourseSettingsFragment();

			// load fragment ID into fragment arguments
			Bundle bundle = new Bundle();
			bundle.putString(CourseSettingsFragment.KEY_ID, course.getId());
			fragment.setArguments(bundle);

			// load the course settings fragment
			getFragmentManager().beginTransaction()
					.replace(R.id.fragmentPlaceHolder, fragment)
					.addToBackStack(null)
					.commit();
		});
	}

	private void addRemovePack(Pack pack) {
		if (course.getPacks().contains(pack)) {
			realm.executeTransaction(r -> {
				course.getPacks().remove(pack);
			});
		} else {
			realm.executeTransaction(r -> {
				course.getPacks().add(pack);
			});
		}
		adapter.notifyDataSetChanged();
	}
}
