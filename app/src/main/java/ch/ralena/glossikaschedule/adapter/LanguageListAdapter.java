package ch.ralena.glossikaschedule.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.object.Schedule;
import io.reactivex.subjects.PublishSubject;
import io.realm.RealmResults;

public class LanguageListAdapter extends RecyclerView.Adapter<LanguageListAdapter.ViewHolder> {

	PublishSubject<Schedule> scheduleSubject = PublishSubject.create();

	public PublishSubject<Schedule> asObservable() {
		return scheduleSubject;
	}

	private Context context;
	private RealmResults<Schedule> schedules;

	public LanguageListAdapter(Context context, RealmResults<Schedule> schedules) {
		this.context = context;
		this.schedules = schedules;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_language, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if (position < getItemCount())
			holder.bindView(schedules.get(position));
	}

	@Override
	public int getItemCount() {
		return schedules.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView languageName;
		private TextView scheduleType;
		private ImageView flagImage;
		private Schedule schedule;

		ViewHolder(View view) {
			super(view);
			this.view = view;
			languageName = view.findViewById(R.id.languageLabel);
			scheduleType = view.findViewById(R.id.scheduleTypeLabel);
			flagImage = view.findViewById(R.id.flagImageView);
			this.view.setOnClickListener(v -> scheduleSubject.onNext(schedule));
		}

		void bindView(Schedule schedule) {
			this.schedule = schedule;
			view.setBackgroundResource(R.drawable.menu_language);
			languageName.setText(schedule.getLanguage());
			scheduleType.setText(schedule.getTitle());
			flagImage.setImageResource(schedule.getLanguageType().getDrawable());
		}
	}
}
