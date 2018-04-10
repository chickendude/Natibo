package ch.ralena.glossikaschedule.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.ralena.glossikaschedule.R;
import io.realm.Realm;

public class CourseAiCreateFragment extends Fragment {
	private static final String TAG = CourseAiCreateFragment.class.getSimpleName();
	public static final String TAG_COURSE_ID = "language_id";

	private Realm realm;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_course_ai_create, container, false);

		realm = Realm.getDefaultInstance();

		return view;
	}
}
