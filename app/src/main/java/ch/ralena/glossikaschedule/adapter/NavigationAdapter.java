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
import io.realm.RealmResults;

public class NavigationAdapter extends RecyclerView.Adapter {
	public interface OnItemClickListener {
		void onNewSchedule();
		void onScheduleClicked(Schedule schedule);
	}

	private static final int TYPE_LANGUAGE = 1;
	private static final int TYPE_ADD_SCHEDULE = 2;
	private Context context;
	private OnItemClickListener listener;
	private RealmResults<Schedule> schedules;
	private int currentPosition;

	public NavigationAdapter(Context context, RealmResults<Schedule> schedules, int currentPosition) {
		this.context = context;
		listener = (OnItemClickListener) context;
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
		if (viewType == TYPE_LANGUAGE) {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_language, parent, false);
			return new ViewHolder(view);
		} else {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_new_schedule, parent, false);
			return new ViewHolderNew(view);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (position < getItemCount() - 1)
			((ViewHolder) holder).bindView(schedules.get(position), position);
	}

	@Override
	public int getItemCount() {
		return schedules.size() + 1;
	}

	@Override
	public int getItemViewType(int position) {
		if (position < getItemCount() - 1) {
			return TYPE_LANGUAGE;
		} else {
			return TYPE_ADD_SCHEDULE;
		}
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
			languageName = (TextView) view.findViewById(R.id.languageLabel);
			scheduleType = (TextView) view.findViewById(R.id.scheduleTypeLabel);
			flagImage = (ImageView) view.findViewById(R.id.flagImageView);
			this.view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ViewGroup parent = (ViewGroup) view.getParent();
					int numViews = parent.getChildCount();
					for (int i = 0; i < numViews; i++) {
						parent.getChildAt(i).setBackgroundResource(R.drawable.menu_language);
					}
					ViewHolder.this.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
					listener.onScheduleClicked(schedule);
				}
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

	private class ViewHolderNew extends RecyclerView.ViewHolder {
		public ViewHolderNew(View itemView) {
			super(itemView);
			itemView.setOnClickListener(view -> listener.onNewSchedule());
		}
	}
}
