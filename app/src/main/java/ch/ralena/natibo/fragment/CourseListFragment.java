package ch.ralena.natibo.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.ralena.natibo.MainActivity;
import ch.ralena.natibo.R;
import ch.ralena.natibo.adapter.CourseListAdapter;
import ch.ralena.natibo.object.Course;
import ch.ralena.natibo.object.Language;
import io.realm.Realm;
import io.realm.RealmResults;

public class CourseListFragment extends Fragment {
	private static final String TAG = CourseListFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "tag_course_id";
	public static final String TAG_START_SESSION = "tag_start_session";

	RealmResults<Course> courses;

	private Realm realm;

	private MainActivity activity;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_course_list, container, false);

		activity = (MainActivity) getActivity();
		activity.enableHomeButton();

		// load schedules from database
		realm = Realm.getDefaultInstance();
		courses = realm.where(Course.class).findAll();

		// check if a course id was passed in, if so move to CourseDetailFragment and add to back stack
		if (getArguments() != null) {
			String courseId = getArguments().getString(TAG_COURSE_ID);
			setArguments(null);
			if (courseId != null) {
				Course course = realm.where(Course.class).equalTo("id", courseId).findFirst();
				if (course != null)
					loadCourseDetailFragment(course);
			}
		}

		// load views
		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		TextView noCoursesText = view.findViewById(R.id.noCoursesText);
		FloatingActionButton fab = view.findViewById(R.id.fab);

		if (courses.size() == 0) {
			noCoursesText.setVisibility(View.VISIBLE);
			recyclerView.setVisibility(View.GONE);
		} else {
			// hide "No Courses" text
			noCoursesText.setVisibility(View.GONE);
			recyclerView.setVisibility(View.VISIBLE);

			// set up recyclerlist and adapter
			CourseListAdapter adapter = new CourseListAdapter(courses);
			recyclerView.setAdapter(adapter);
			RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
			recyclerView.setLayoutManager(layoutManager);

			adapter.asObservable().subscribe(this::loadCourseDetailFragment);
		}

		// set up FAB
		fab.setOnClickListener(v -> loadCourseCreateFragment());

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		((MainActivity) getActivity()).setNavigationDrawerItemChecked(R.id.nav_courses);
		getActivity().setTitle(getString(R.string.courses));
	}

	private void loadCourseDetailFragment(Course course) {
		// load new fragment
		CourseDetailFragment fragment = new CourseDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString(CourseDetailFragment.TAG_COURSE_ID, course.getId());
		fragment.setArguments(bundle);
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.addToBackStack(null)
				.commit();
	}

	private void loadCourseCreateFragment() {
		if (realm.where(Language.class).count() == 0) {
			activity.snackBar(R.string.no_languages);
		} else {
			// load new fragment
			CoursePickLanguageFragment fragment = new CoursePickLanguageFragment();
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.fragmentPlaceHolder, fragment)
					.addToBackStack(null)
					.commit();
		}
	}
}
