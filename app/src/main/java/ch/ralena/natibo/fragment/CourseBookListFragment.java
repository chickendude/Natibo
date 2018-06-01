package ch.ralena.natibo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ch.ralena.natibo.MainActivity;
import ch.ralena.natibo.R;
import ch.ralena.natibo.adapter.CourseBookAdapter;
import ch.ralena.natibo.object.Course;
import ch.ralena.natibo.object.Pack;
import io.realm.Realm;
import io.realm.RealmResults;

public class CourseBookListFragment extends Fragment {
	private static final String TAG = CourseBookListFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "tag_course_id";

	RealmResults<Course> courses;
	Course course;


	private Realm realm;

	private MainActivity activity;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_course_book_list, container, false);

		activity = (MainActivity) getActivity();
		activity.enableHomeButton();

		// load course that was passed in via bundle from the database
		realm = Realm.getDefaultInstance();

		// check if a proper course id was passed in, if not exit
		loadCourse();

		// set up recyclerlist and adapter
		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		CourseBookAdapter adapter = new CourseBookAdapter(course.getTargetPacks());
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
		recyclerView.setLayoutManager(layoutManager);

		adapter.asObservable().subscribe(this::loadSentenceListFragment);

		return view;
	}

	private void loadCourse() {
		if (getArguments() != null) {
			String courseId = getArguments().getString(TAG_COURSE_ID);
			setArguments(null);
			if (courseId != null) {
				course = realm.where(Course.class).equalTo("id", courseId).findFirst();
			}
		}

		if (course == null) {
			Toast.makeText(activity, "Invalid course", Toast.LENGTH_SHORT).show();
			activity.onBackPressed();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		activity.setNavigationDrawerItemChecked(R.id.nav_courses);
		activity.setTitle(getString(R.string.books));
	}

	private void loadSentenceListFragment(Pack pack) {
		// load new fragment
		SentenceListFragment fragment = new SentenceListFragment();

		Bundle bundle = new Bundle();
		bundle.putString(SentenceListFragment.TAG_PACK_ID, pack.getId());
		bundle.putString(SentenceListFragment.TAG_LANGUAGE_ID, course.getTargetLanguage().getLanguageId());
		fragment.setArguments(bundle);

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.addToBackStack(null)
				.commit();
	}
}
