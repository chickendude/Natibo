package ch.ralena.glossikaschedule.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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

public class LanguageListAdapter extends RecyclerView.Adapter {

	PublishSubject<Schedule> scheduleSubject = PublishSubject.create();

	public PublishSubject<Schedule> asObservable() {
		return scheduleSubject;
	}

	private Context context;
	private RealmResults<Schedule> schedules;
	private int currentPosition;

	public LanguageListAdapter(Context context, RealmResults<Schedule> schedules) {
		this.context = context;
		this.schedules = schedules;
		this.currentPosition = currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view;
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_language, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (position < getItemCount() - 1)
			((ViewHolder) holder).bindView(schedules.get(position), position);
	}

	@Override
	public int getItemCount() {
		return schedules.size();
	}

	private class ViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView languageName;
		private TextView scheduleType;
		private ImageView flagImage;
		private Schedule schedule;

		public ViewHolder(View view) {
			super(view);
			this.view = view;
			languageName = view.findViewById(R.id.languageLabel);
			scheduleType = view.findViewById(R.id.scheduleTypeLabel);
			flagImage = view.findViewById(R.id.flagImageView);
			this.view.setOnClickListener(v -> {
				ViewGroup parent = (ViewGroup) v.getParent();
				int numViews = parent.getChildCount();
				for (int i = 0; i < numViews; i++) {
					parent.getChildAt(i).setBackgroundResource(R.drawable.menu_language);
				}
				ViewHolder.this.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
				scheduleSubject.onNext(schedule);
			});
		}

		public void bindView(Schedule schedule, int position) {
			this.schedule = schedule;
			// highlight currently selected menu item
			if (currentPosition == position) {
				view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
			} else {
				view.setBackgroundResource(R.drawable.menu_language);
			}
			languageName.setText(schedule.getLanguage());
			scheduleType.setText(schedule.getTitle());
			flagImage.setImageResource(schedule.getLanguageType().getDrawable());
		}
	}
}
