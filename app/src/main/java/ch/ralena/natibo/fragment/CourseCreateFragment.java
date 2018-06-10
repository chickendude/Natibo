package ch.ralena.natibo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import ch.ralena.natibo.MainActivity;
import ch.ralena.natibo.R;
import ch.ralena.natibo.object.Language;
import io.realm.Realm;
import io.realm.RealmResults;

public class CourseCreateFragment extends Fragment {
	private static final String TAG = CourseCreateFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "language_id";

	RealmResults<Language> languages;
	Language baseLanguage;
	Language targetLanguage;

	private Realm realm;

	Spinner baseSpinner;
	Spinner targetSpinner;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_course_create, container, false);

		// switch to back button
		MainActivity activity = (MainActivity) getActivity();
		activity.enableBackButton();
		activity.setTitle("Create a Course");

		realm = Realm.getDefaultInstance();

		languages = realm.where(Language.class).findAll();
		String[] languagesArray = new String[languages.size()];

		for (int i = 0; i < languagesArray.length; i++) {
			languagesArray[i] = languages.get(i).getLongName();
		}

		// language spinners
		baseSpinner = view.findViewById(R.id.baseSpinner);
		targetSpinner = view.findViewById(R.id.targetSpinner);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, languagesArray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		baseSpinner.setAdapter(adapter);
		targetSpinner.setAdapter(adapter);

		// make sure they are different
		if (languagesArray.length > 1)
			targetSpinner.setSelection(1);

		baseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == targetSpinner.getSelectedItemPosition()) {
					int newTargetId = languages.indexOf(baseLanguage);
					targetSpinner.setSelection(newTargetId);
				}
				baseLanguage = languages.get(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		targetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == baseSpinner.getSelectedItemPosition()) {
					int newBaseId = languages.indexOf(targetLanguage);
					baseSpinner.setSelection(newBaseId);
				}
				targetLanguage = languages.get(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		// buttons
		Button aiButton = view.findViewById(R.id.prepackagedButton);
		Button byocButton = view.findViewById(R.id.byocButton);

		aiButton.setOnClickListener(v -> loadPrepackagedCourseFragment());
		byocButton.setOnClickListener(v -> loadByocCourseFragment());

		return view;
	}

	private boolean checkLanguagesSet() {
		return languages.contains(baseLanguage) && languages.contains(targetLanguage) && targetLanguage != baseLanguage;
	}

	private void loadByocCourseFragment() {
		if (!checkLanguagesSet())
			return;
	}

	private void loadPrepackagedCourseFragment() {
		if (!checkLanguagesSet())
			return;
		CoursePrepackagedCreateFragment fragment = new CoursePrepackagedCreateFragment();
		Bundle bundle = new Bundle();
		bundle.putString(CoursePrepackagedCreateFragment.TAG_BASE_LANGUAGE, baseLanguage.getLanguageId());
		bundle.putString(CoursePrepackagedCreateFragment.TAG_TARGET_LANGUAGE, targetLanguage.getLanguageId());
		fragment.setArguments(bundle);
		getFragmentManager().beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.addToBackStack(null)
				.commit();
	}
}
