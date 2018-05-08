package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.ralena.glossikaschedule.MainActivity;
import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.adapter.LanguageListAdapter;
import ch.ralena.glossikaschedule.object.Language;
import io.reactivex.functions.Consumer;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class LanguageListFragment extends Fragment {
	private static final String TAG = LanguageListFragment.class.getSimpleName();

	RealmResults<Language> languages;

	private Realm realm;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_language_list, container, false);

		// load schedules from database
		realm = Realm.getDefaultInstance();
		languages = realm.where(Language.class).findAll();

		// load views
		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		TextView noLanguagesText = view.findViewById(R.id.noCoursesText);
		FloatingActionButton fab = view.findViewById(R.id.fab);

		if (languages.size() == 0) {
			noLanguagesText.setVisibility(View.VISIBLE);
			recyclerView.setVisibility(View.GONE);
		} else {
			// check if there are any empty languages
			deleteEmptyLanguages();

			// hide "No Courses" text
			noLanguagesText.setVisibility(View.GONE);
			recyclerView.setVisibility(View.VISIBLE);

			// set up recyclerlist and adapter
			LanguageListAdapter adapter = new LanguageListAdapter(languages);
			recyclerView.setAdapter(adapter);
			RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
			recyclerView.setLayoutManager(layoutManager);

			adapter.asObservable().subscribe(this::loadLanguageDetailFragment);

			adapter.asObservable().subscribe(new Consumer<Language>() {
				@Override
				public void accept(Language language) throws Exception {

				}
			});
		}

		// set up FAB
		fab.setOnClickListener(v -> ((MainActivity) getActivity()).importLanguagPack());

		return view;
	}

	private void deleteEmptyLanguages() {
		// find all empty languages
		RealmList<Language> emptyLanguages = new RealmList<>();
		for (Language language : languages) {
			if (language.getLanguageType() == null || language.getLanguageId().equals(""))
				emptyLanguages.add(language);
		}
		// delete the packs and languages themselves
		realm.executeTransaction(r -> {
			for (Language language : emptyLanguages) {
				if (language.getPacks() != null)
					language.getPacks().deleteAllFromRealm();
				language.deleteFromRealm();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		((MainActivity) getActivity()).setNavigationDrawerItemChecked(R.id.nav_languages);
		getActivity().setTitle(getString(R.string.languages));
	}

	private void loadLanguageDetailFragment(Language language) {
		// load new fragment
		LanguageDetailFragment fragment = new LanguageDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString(LanguageDetailFragment.TAG_LANGUAGE_ID, language.getLanguageId());
		fragment.setArguments(bundle);
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.addToBackStack(null)
				.commit();
	}

}
