package ch.ralena.glossikaschedule.fragment;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.utils.GLSImporter;

public class LanguageImportFragment extends Fragment {
	public static final String EXTRA_URI = "extra_uri";
	public static final int ACTION_OPENING_FILE = 0;
	public static final int ACTION_COUNTING_SENTENCES = 1;
	public static final int ACTION_READING_SENTENCES = 2;
	public static final int ACTION_EXTRACTING_TEXT = 3;
	public static final int ACTION_EXTRACTING_AUDIO = 4;
	public static final int ACTION_EXIT = 5;

	ProgressBar progressBar;
	TextView fileNameText;
	TextView actionText;
	TextView counterText;
	TextView totalText;
	TextView dividerBarLabel;

	int curAction;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_language_import, container, false);
		progressBar = view.findViewById(R.id.progressBar);
		fileNameText = view.findViewById(R.id.fileNameText);
		actionText = view.findViewById(R.id.actionText);
		counterText = view.findViewById(R.id.counterText);
		totalText = view.findViewById(R.id.totalText);
		dividerBarLabel = view.findViewById(R.id.dividerBarLabel);
		return view;
	}

	@SuppressLint("CheckResult")
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Uri uri = getArguments().getParcelable(EXTRA_URI);
		GLSImporter importer = new GLSImporter();

		// the total counter
		importer.totalObservable().subscribe(
				total -> {
					if (getActivity() != null)
						getActivity().runOnUiThread(
								() -> {
									totalText.setText(String.valueOf(total));
									progressBar.setMax(total);
								}
						);
				}
		);

		// the progress counter
		importer.progressObservable().subscribe(
				progress -> {
					if (getActivity() != null)
						getActivity().runOnUiThread(
								() -> {
									counterText.setText(String.valueOf(progress));
									progressBar.setProgress(progress);
									// if pack has finished loading, go to the language list screen.
									if (curAction == ACTION_EXTRACTING_AUDIO && progressBar.getMax() == progress) {
										loadLanguageListFragment();
									}
								}
						);
				}
		);

		// load filename
		importer.fileNameSubject().subscribe(
				filename -> {
					fileNameText.setVisibility(View.VISIBLE);
					fileNameText.setText(filename);
				});

		// the currently happening action
		importer.actionSubject().subscribe(
				actionId -> {
					if (getActivity() != null)
						getActivity().runOnUiThread(() -> {
							curAction = actionId;
							switch (actionId) {
								case ACTION_OPENING_FILE:
									openFile();
									break;
								case ACTION_COUNTING_SENTENCES:
									countSentences();
									break;
								case ACTION_READING_SENTENCES:
									readSentences();
									break;
								case ACTION_EXTRACTING_TEXT:
									extractText();
									break;
								case ACTION_EXTRACTING_AUDIO:
									extractAudio();
									break;
								case ACTION_EXIT:
									loadLanguageListFragment();
									break;
							}
						});
				});

		importer.importPack(getContext(), uri);
	}

	private void extractAudio() {
		actionText.getResources().getString(R.string.extracting_sentence_audio);
	}

	private void readSentences() {
		actionText.getResources().getString(R.string.reading_sentences);
		counterText.setVisibility(View.GONE);
		dividerBarLabel.setVisibility(View.GONE);
		totalText.setVisibility(View.GONE);
	}

	private void extractText() {
		actionText.setText(getResources().getString(R.string.extracting_sentence_text));
	}

	private void countSentences() {
		actionText.setText(getResources().getString(R.string.counting_sentences));

		counterText.setVisibility(View.VISIBLE);
		totalText.setVisibility(View.VISIBLE);
		dividerBarLabel.setVisibility(View.VISIBLE);
	}

	private void openFile() {
		actionText.setText(getResources().getString(R.string.opening_file));

		fileNameText.setVisibility(View.VISIBLE);
		actionText.setVisibility(View.VISIBLE);
		counterText.setVisibility(View.GONE);
		totalText.setVisibility(View.GONE);
		dividerBarLabel.setVisibility(View.GONE);
	}

	private void loadLanguageListFragment() {
		LanguageListFragment fragment = new LanguageListFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.commit();
	}
}
