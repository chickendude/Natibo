package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import ch.ralena.glossikaschedule.R;

public class SentenceListFragment extends Fragment {
	public static final String EXTRA_TYPE = "extra_type";
	public static final int TYPE_LOAD = 0;
	public static final int TYPE_IMPORT = 1;
	public static final String EXTRA_URI = "extra_uri";
	public static final String EXTRA_LANGUAGE = "extra_language";

	ProgressBar progressBar;
	RecyclerView recyclerView;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sentence_list, container, false);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

	}
}
