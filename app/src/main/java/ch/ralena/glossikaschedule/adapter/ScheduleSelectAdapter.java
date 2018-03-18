package ch.ralena.glossikaschedule.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.data.ScheduleType;

public class ScheduleSelectAdapter extends RecyclerView.Adapter<ScheduleSelectAdapter.ScheduleViewHolder> {
	public interface OnScheduleSelectedListener {
		void onScheduleSelected(ScheduleType schedule);
	}

	OnScheduleSelectedListener listener;

	Map<Integer, List<ScheduleType>> schedules;
	List<Integer> minutesList;
	int currentSelection;

	public ScheduleSelectAdapter(TreeMap<Integer, List<ScheduleType>> schedules) {
		minutesList = new ArrayList<>();
		for (Integer integer : schedules.keySet()) {
			minutesList.add(integer);
		}
		this.schedules = schedules;
	}

	@Override
	public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_minutes, parent, false);
		listener = (OnScheduleSelectedListener) parent.getContext();
		return new ScheduleViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ScheduleViewHolder holder, int position) {
		holder.bindView(position);
	}

	@Override
	public int getItemCount() {
		return schedules.size();
	}

	class ScheduleViewHolder extends RecyclerView.ViewHolder {
		TextView minutesLabel;

		ScheduleViewHolder(View itemView) {
			super(itemView);
			minutesLabel = itemView.findViewById(R.id.minutesLabel);
		}

		private void bindView(int position) {
			itemView.setOnClickListener(view -> {
				notifyItemChanged(currentSelection);
				notifyItemChanged(position);
				currentSelection = position;
			});

			if (position == currentSelection) {
				itemView.setBackgroundResource(R.drawable.schedule_minutes_selected);
			} else {
				itemView.setBackgroundResource(R.drawable.schedule_minutes);
			}

//			itemView.setOnClickListener(view -> {
////				selectedLanguage = schedule;
//				notifyDataSetChanged();
////				listener.onScheduleSelected(minutes);
//			});
			minutesLabel.setText(minutesList.get(position) + "");
		}
	}
}
