package ch.ralena.glossikaschedule.fragment;

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

	ProgressBar progressBar;
	TextView sentenceNumberText;
	TextView totalSentencesText;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_language_import, container, false);
		progressBar = view.findViewById(R.id.progressBar);
		sentenceNumberText = view.findViewById(R.id.sentenceNumberText);
		totalSentencesText = view.findViewById(R.id.totalSentencesText);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Uri uri = getArguments().getParcelable(EXTRA_URI);
		GLSImporter importer = new GLSImporter();
		importer.totalObservable().subscribe(
				total -> getActivity().runOnUiThread(
						() -> {
							totalSentencesText.setText(String.valueOf(total));
							progressBar.setMax(total);
						}
				)
		);
		importer.progressObservable().subscribe(
				progress -> {
					if (getActivity() != null)
						getActivity().runOnUiThread(
								() -> {
									sentenceNumberText.setText(String.valueOf(progress));
									progressBar.setProgress(progress);
									// if pack has finished loading, go to the language list screen.
									if (progressBar.getMax() == progress) {
										LanguageListFragment fragment = new LanguageListFragment();
										getFragmentManager().beginTransaction()
												.replace(R.id.fragmentPlaceHolder, fragment)
												.commit();
									}
								}
						);
				}
		);
		importer.importPack(

				getContext(), uri);
	}
}
