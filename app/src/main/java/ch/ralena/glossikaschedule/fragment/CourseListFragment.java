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

import ch.ralena.glossikaschedule.MainActivity;
import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.adapter.CourseListAdapter;
import ch.ralena.glossikaschedule.object.Course;
import io.realm.Realm;
import io.realm.RealmResults;

public class CourseListFragment extends Fragment {
	private static final String TAG = CourseListFragment.class.getSimpleName();

	RealmResults<Course> courses;

	private Realm realm;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_language_list, container, false);

		// load schedules from database
		realm = Realm.getDefaultInstance();
		courses = realm.where(Course.class).findAll();

		if (courses.size() == 0) {

		}

		// set up recyclerlist and adapter
		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		CourseListAdapter adapter = new CourseListAdapter(courses);
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(layoutManager);

		adapter.asObservable().subscribe(this::loadCourseDetailFragment);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) getActivity()).setNavigationDrawerItemChecked(R.id.nav_languages);
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

}
