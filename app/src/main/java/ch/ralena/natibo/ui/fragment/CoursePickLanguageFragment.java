package ch.ralena.natibo.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ch.ralena.natibo.ui.MainActivity;
import ch.ralena.natibo.R;
import ch.ralena.natibo.ui.adapter.CourseAvailableLanguagesAdapter;
import ch.ralena.natibo.ui.adapter.CourseSelectedLanguagesAdapter;
import ch.ralena.natibo.ui.callback.ItemTouchHelperCallback;
import ch.ralena.natibo.data.room.object.Language;
import io.realm.Realm;

public class CoursePickLanguageFragment extends Fragment implements CourseSelectedLanguagesAdapter.OnDragListener {
	public static final String TAG = CoursePickLanguageFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "language_id";

	ArrayList<Language> availableLanguages;
	ArrayList<Language> selectedLanguages;

	private Realm realm;

	RecyclerView availableLanguagesRecyclerView;
	RecyclerView selectedLanguagesRecyclerView;
	CourseAvailableLanguagesAdapter availableAdapter;
	CourseSelectedLanguagesAdapter selectedAdapter;

	private ItemTouchHelper itemTouchHelper;
	private MenuItem checkMenu;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_course_pick_language, container, false);

		// switch to back button
		MainActivity activity = (MainActivity) getActivity();
		activity.enableBackButton();
		activity.setTitle(getString(R.string.select_languages));

		setHasOptionsMenu(true);

		realm = Realm.getDefaultInstance();

		availableLanguages = Language.getLanguagesSorted(realm);

		selectedLanguages = new ArrayList<>();

		// recycler views
		loadRecyclerViews(view);

		return view;
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
				loadCoursePreparationFragment();
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadRecyclerViews(View view) {
		availableLanguagesRecyclerView = view.findViewById(R.id.availableLanguagesRecyclerView);
		selectedLanguagesRecyclerView = view.findViewById(R.id.selectedLanguagesRecyclerView);

		availableAdapter = new CourseAvailableLanguagesAdapter(availableLanguages);
		availableAdapter.asObservable().subscribe(this::availableLanguageClicked);
		availableLanguagesRecyclerView.setAdapter(availableAdapter);
		availableLanguagesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

		selectedAdapter = new CourseSelectedLanguagesAdapter(selectedLanguages, this);
		selectedLanguagesRecyclerView.setAdapter(selectedAdapter);
		selectedLanguagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(selectedAdapter, false);
		itemTouchHelper = new ItemTouchHelper(callback);
		itemTouchHelper.attachToRecyclerView(selectedLanguagesRecyclerView);
	}

	private void availableLanguageClicked(Language language) {
		if (selectedLanguages.contains(language)) {
			selectedLanguages.remove(language);
		} else {
			selectedLanguages.add(language);
		}
		checkMenu.setVisible(selectedLanguages.size() > 0);
		selectedAdapter.notifyDataSetChanged();
	}

	private void loadCoursePreparationFragment() {
		CoursePreparationFragment fragment = new CoursePreparationFragment();

		// add language ids in a bundle
		Bundle bundle = new Bundle();
		ArrayList<String> languageIds = new ArrayList<>();
		for (Language language : selectedLanguages) {
			languageIds.add(language.getLanguageId());
		}
		bundle.putStringArrayList(CoursePreparationFragment.TAG_LANGUAGE_IDS, languageIds);
		fragment.setArguments(bundle);

		getFragmentManager().beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void onStartDrag(RecyclerView.ViewHolder holder) {
		itemTouchHelper.startDrag(holder);
	}
}
