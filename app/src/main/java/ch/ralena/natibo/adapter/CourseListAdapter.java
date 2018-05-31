package ch.ralena.natibo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ch.ralena.natibo.R;
import ch.ralena.natibo.object.Course;
import io.reactivex.subjects.PublishSubject;
import io.realm.RealmResults;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.ViewHolder> {

	PublishSubject<Course> courseSubject = PublishSubject.create();

	public PublishSubject<Course> asObservable() {
		return courseSubject;
	}

	private RealmResults<Course> courses;

	public CourseListAdapter(RealmResults<Course> courses) {
		this.courses = courses;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_list, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if (position < getItemCount())
			holder.bindView(courses.get(position));
	}

	@Override
	public int getItemCount() {
		return courses.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView languageName;
		private TextView courseTitle;
		private TextView numReps;
		private ImageView flagImage;
		private Course course;

		ViewHolder(View view) {
			super(view);
			this.view = view;
			languageName = view.findViewById(R.id.languageLabel);
			courseTitle = view.findViewById(R.id.courseTitleLabel);
			numReps= view.findViewById(R.id.numRepsLabel);
			flagImage = view.findViewById(R.id.flagImageView);
			this.view.setOnClickListener(v -> courseSubject.onNext(course));
		}

		void bindView(Course course) {
			this.course = course;
			languageName.setText(course.getTargetLanguage().getLongName());
			courseTitle.setText(course.getTitle());
			numReps.setText(String.format("%d reps", course.getTotalReps()));
			flagImage.setImageResource(course.getTargetLanguage().getLanguageType().getDrawable());
		}
	}
}
