package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.ralena.glossikaschedule.NewScheduleActivity;
import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.adapter.LanguageSelectAdapter;
import ch.ralena.glossikaschedule.data.LanguageData;
import ch.ralena.glossikaschedule.data.LanguageType;

public class NewScheduleLanguageFragment extends Fragment {
	private LanguageType selectedLanguage;
	NewScheduleActivity activity;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// get activity
		activity = ((NewScheduleActivity) getActivity());
		// update action bar
		ActionBar actionBar = activity.getSupportActionBar();
		actionBar.setTitle("Which language are you studying?");
		actionBar.setDisplayHomeAsUpEnabled(false);

		// load view
		View view = inflater.inflate(R.layout.fragment_new_schedule_language, container, false);

		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		LanguageSelectAdapter languageSelectAdapter = new LanguageSelectAdapter(LanguageData.languages, selectedLanguage);
		languageSelectAdapter.asObservable().subscribe(language -> {
			activity.updateLanguage(language);
			selectedLanguage = language;
			NewScheduleScheduleFragment fragment = new NewScheduleScheduleFragment();
			getFragmentManager().beginTransaction()
					.replace(R.id.fragmentContainer, fragment)
					.addToBackStack(null)
					.commit();
		});
		recyclerView.setAdapter(languageSelectAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		return view;
	}
}
