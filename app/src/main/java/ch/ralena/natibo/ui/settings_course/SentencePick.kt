package ch.ralena.natibo.ui.settings_course;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.Locale;

import ch.ralena.natibo.R;
import ch.ralena.natibo.data.room.object.Course;
import ch.ralena.natibo.data.room.object.Day;
import ch.ralena.natibo.data.room.object.Language;
import ch.ralena.natibo.data.room.object.Pack;
import ch.ralena.natibo.data.room.object.Sentence;
import ch.ralena.natibo.ui.MainActivity;
import ch.ralena.natibo.ui.adapter.PickSentenceAdapter;
import io.realm.Realm;
import io.realm.RealmList;

public class CoursePickSentenceFragment extends Fragment {
	private static final String TAG = CoursePickSentenceFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "course_id";

	private Language language;
	private Course course;
	private RealmList<Sentence> sentences;
	private Sentence curSentence;

	private Realm realm;

	private MediaPlayer mediaPlayer;
	private RecyclerView recyclerView;
	private SeekBar seekBar;
	private SeekBar.OnSeekBarChangeListener seekBarChangeListener;
	private MenuItem checkMenu;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sentence_list, container, false);

		setHasOptionsMenu(true);

		realm = Realm.getDefaultInstance();
		mediaPlayer = new MediaPlayer();

		// load language and pack from database
		String courseId = getArguments().getString(TAG_COURSE_ID, null);

		course = realm.where(Course.class).equalTo("id", courseId).findFirst();
		language = course.getLanguages().last();

		// load language name
		getActivity().setTitle(language.getLanguageType().getName());

		loadSentences();

		// prepare seekbar
		loadSeekBar(view);

		// set up recyclerlist and adapter
		loadRecyclerView(view);

		return view;
	}

	private void loadRecyclerView(View view) {
		recyclerView = view.findViewById(R.id.recyclerView);
		PickSentenceAdapter adapter = new PickSentenceAdapter(language.getLanguageId(), sentences);
		recyclerView.setAdapter(adapter);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(layoutManager);

		adapter.asObservable().subscribe(this::onSentenceClicked);

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
	}

	private void loadSeekBar(View view) {
		seekBar = view.findViewById(R.id.seekbar);
		seekBar.setMax(sentences.size());

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
	}

	private void loadSentences() {
		sentences = new RealmList<>();
		for (Pack pack : course.getPacks()) {
			// use a sentence with index of -1 to separate the books
			Sentence sentence = new Sentence();
			sentence.setIndex(-1);
			sentence.setText(pack.getBook());
			sentences.add(sentence);
			sentences.addAll(pack.getSentences());
		}
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.check_toolbar, menu);
		checkMenu = menu.getItem(0);
		checkMenu.setVisible(false);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_confirm:
				setStartingSentence();
		}
		return super.onOptionsItemSelected(item);
	}

	private void setStartingSentence() {
		Day curDay = course.getCurrentDay();
		if (curDay != null
				&& !curDay.isCompleted()
				&& curDay.getTotalReviews() != curDay.getNumReviewsLeft()) {
			AlertDialog inProgressDialog = new AlertDialog.Builder(getContext())
					.setTitle(R.string.session_in_progress)
					.setMessage(R.string.reset_session)
					.setPositiveButton(android.R.string.yes, ((dialog, which) -> updateStartingCourse()))
					.setNegativeButton(android.R.string.no, ((dialog, which) -> {
					}))
					.create();
			inProgressDialog.show();
		} else {
			updateStartingCourse();
		}

	}

	private void updateStartingCourse() {
		course.setStartingSentenceForAllSchedules(realm, curSentence);
		String book = "";
		for (Pack pack : course.getPacks()) {
			if (pack.getSentences().contains(curSentence)) {
				book = pack.getBook();
				break;
			}
		}
		Toast.makeText(getContext(), String.format(Locale.getDefault(), getString(R.string.sentence_set), book, curSentence.getIndex()), Toast.LENGTH_SHORT).show();
		getActivity().onBackPressed();
	}

	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) getActivity()).setMenuToLanguages();
	}

	private void onSentenceClicked(Sentence sentence) {
		curSentence = sentence;
		checkMenu.setVisible(true);
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
