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
import android.widget.ImageView;
import android.widget.TextView;

import ch.ralena.glossikaschedule.MainActivity;
import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.adapter.LanguageDetailAdapter;
import ch.ralena.glossikaschedule.object.Language;
import ch.ralena.glossikaschedule.object.Pack;
import io.realm.Realm;
import io.realm.RealmList;

public class LanguageDetailFragment extends Fragment {
	private static final String TAG = LanguageDetailFragment.class.getSimpleName();
	public static final String TAG_LANGUAGE_ID = "language_id";

	Language language;
	RealmList<Pack> packs;

	private Realm realm;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_language_detail, container, false);

		String id = getArguments().getString(TAG_LANGUAGE_ID);
		// load schedules from database
		realm = Realm.getDefaultInstance();
		language = realm.where(Language.class).equalTo("languageId", id).findFirst();
		packs = language.getPacks();

		// load flag image
		ImageView flagImage = view.findViewById(R.id.flagImageView);
		flagImage.setImageResource(language.getLanguageType().getDrawable());

		// load language name
		TextView languageLabel = view.findViewById(R.id.languageLabel);
		languageLabel.setText(language.getLanguageType().getName());

		// set up recyclerlist and adapter
		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		LanguageDetailAdapter adapter = new LanguageDetailAdapter(getContext(), packs);
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(layoutManager);

		adapter.asObservable().subscribe(this::loadMainFragment);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) getActivity()).setNavigationDrawerItemChecked(R.id.nav_languages);
	}

	private void loadMainFragment(Pack pack) {
		// load new fragment
//		MainFragment mainFragment = new MainFragment();
//		Bundle bundle = new Bundle();
//		bundle.putString(TAG_LANGUAGE_ID, schedule.getId());
//		mainFragment.setArguments(bundle);
//		getFragmentManager()
//				.beginTransaction()
//				.replace(R.id.fragmentPlaceHolder, mainFragment, MainFragment.MAIN_FRAGMENT_TAG)
//				.commit();
	}

}
