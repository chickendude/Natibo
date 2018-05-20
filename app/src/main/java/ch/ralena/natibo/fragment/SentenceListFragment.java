package ch.ralena.natibo.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import ch.ralena.natibo.MainActivity;
import ch.ralena.natibo.R;
import ch.ralena.natibo.adapter.SentenceListAdapter;
import ch.ralena.natibo.object.Language;
import ch.ralena.natibo.object.Pack;
import ch.ralena.natibo.object.Sentence;
import io.realm.Realm;

public class SentenceListFragment extends Fragment {
	private static final String TAG = SentenceListFragment.class.getSimpleName();
	public static final String TAG_LANGUAGE_ID = "language_id";
	public static final String TAG_PACK_ID = "pack_id";

	Language language;
	Pack pack;

	private Realm realm;

	private MediaPlayer mediaPlayer;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sentence_list, container, false);

		realm = Realm.getDefaultInstance();
		mediaPlayer = new MediaPlayer();

		// load language and pack from database
		String languageId = getArguments().getString(TAG_LANGUAGE_ID);
		String packId = getArguments().getString(TAG_PACK_ID);
		language = realm.where(Language.class).equalTo("languageId", languageId).findFirst();
		pack = realm.where(Pack.class).equalTo("id", packId).findFirst();

		// load language name
		getActivity().setTitle(language.getLanguageType().getName());

		// set up recyclerlist and adapter
		RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
		SentenceListAdapter adapter = new SentenceListAdapter(language.getLanguageId(), pack.getSentences());
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(layoutManager);

		adapter.asObservable().subscribe(this::loadSentenceDetailFragment);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) getActivity()).setNavigationDrawerItemChecked(R.id.nav_languages);
	}

	private void loadSentenceDetailFragment(Sentence sentence) {
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(sentence.getUri());
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
