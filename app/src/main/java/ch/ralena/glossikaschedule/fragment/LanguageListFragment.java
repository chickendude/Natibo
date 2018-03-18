package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.adapter.LanguageListAdapter;
import ch.ralena.glossikaschedule.object.Schedule;
import io.realm.Realm;
import io.realm.RealmResults;

public class LanguageListFragment extends Fragment {
	private static final String TAG = LanguageListFragment.class.getSimpleName();
	public static final String TAG_SCHEDULE_ID = "schedule_id";

	RealmResults<Schedule> schedules;

	private Realm realm;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_languages, container, false);

		// load schedules from database
		realm = Realm.getDefaultInstance();
		schedules = realm.where(Schedule.class).findAll();

		if (schedules.size() == 0) {

		}

		Log.d(TAG, "" + schedules.size());

		// set up recyclerlist and adapter
		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		LanguageListAdapter adapter = new LanguageListAdapter(getContext(), schedules);
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(layoutManager);

		adapter.asObservable().subscribe(this::loadMainFragment);

		return view;
	}

	private void loadMainFragment(Schedule schedule) {
		// load new fragment
		MainFragment mainFragment = new MainFragment();
		Bundle bundle = new Bundle();
		bundle.putString(TAG_SCHEDULE_ID, schedule.getId());
		mainFragment.setArguments(bundle);
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, mainFragment, MainFragment.MAIN_FRAGMENT_TAG)
				.commit();
	}

}
