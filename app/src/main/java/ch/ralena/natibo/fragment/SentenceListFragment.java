package ch.ralena.natibo.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

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
	public static final String TAG_BASE_PACK_ID = "base_pack_id";
	public static final String TAG_TARGET_PACK_ID = "target_pack_id";

	Language language;
	Pack basePack;

	private Realm realm;

	private MediaPlayer mediaPlayer;
	private RecyclerView recyclerView;
	private SeekBar.OnSeekBarChangeListener seekBarChangeListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sentence_list, container, false);

		realm = Realm.getDefaultInstance();
		mediaPlayer = new MediaPlayer();

		// load language and pack from database
		String languageId = getArguments().getString(TAG_LANGUAGE_ID);
		String basePackId = getArguments().getString(TAG_BASE_PACK_ID);
		language = realm.where(Language.class).equalTo("languageId", languageId).findFirst();
		basePack = realm.where(Pack.class).equalTo("id", basePackId).findFirst();

		// load language name
		getActivity().setTitle(language.getLanguageType().getName());

		// prepare seekbar
		SeekBar seekBar = view.findViewById(R.id.seekbar);
		seekBar.setMax(basePack.getSentences().size());

		seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				recyclerView.scrollToPosition(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				recyclerView.scrollToPosition(seekBar.getProgress());
			}
		};

		seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

		// set up recyclerlist and adapter
		recyclerView = view.findViewById(R.id.recyclerView);
		SentenceListAdapter adapter = new SentenceListAdapter(language.getLanguageId(), basePack.getSentences());
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(layoutManager);

		adapter.asObservable().subscribe(this::playSentence);

		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				seekBar.setOnSeekBarChangeListener(null);
				seekBar.setProgress(((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition());
				seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
			}

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) getActivity()).setNavigationDrawerItemChecked(R.id.nav_languages);
	}

	private void playSentence(Sentence sentence) {
		if (sentence.getUri() == null) {
			Toast.makeText(getContext(), String.format("Audio file not found for '%s'", sentence.getText()), Toast.LENGTH_SHORT).show();
			return;
		}
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
