package ch.ralena.natibo.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import ch.ralena.natibo.R;
import ch.ralena.natibo.callback.ItemTouchHelperCallback;
import ch.ralena.natibo.object.Language;
import io.reactivex.subjects.PublishSubject;

public class CourseSelectedLanguagesAdapter extends RecyclerView.Adapter<CourseSelectedLanguagesAdapter.ViewHolder> implements ItemTouchHelperCallback.ItemTouchHelperAdapter {
	public interface OnDragListener {
		void onStartDrag(RecyclerView.ViewHolder holder);
	}

	PublishSubject<Language> languageSubject = PublishSubject.create();
	OnDragListener dragListener;

	public PublishSubject<Language> asObservable() {
		return languageSubject;
	}

	private ArrayList<Language> languages;

	public CourseSelectedLanguagesAdapter(ArrayList<Language> languages, OnDragListener dragListener) {
		this.languages = languages;
		this.dragListener = dragListener;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_language_list, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bindView(languages.get(position), position);
	}

	@Override
	public int getItemCount() {
		return languages.size();
	}


	@Override
	public boolean onItemMove(int fromPosition, int toPosition) {
		int start, count;
		if (fromPosition < toPosition) {
			start = fromPosition;
			count = toPosition - fromPosition;
			for (int i = fromPosition; i < toPosition; i++) {
				Collections.swap(languages, i, i + 1);
			}
		} else {
			start = toPosition;
			count = fromPosition - toPosition;
			for (int i = fromPosition; i > toPosition; i--) {
				Collections.swap(languages, i, i - 1);
			}
		}
		notifyItemMoved(fromPosition, toPosition);
		notifyItemRangeChanged(start, count + 1);
		return true;
	}

	@Override
	public void onItemDismiss(int position) {
		languages.remove(position);
		notifyItemRemoved(position);
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView languageName;
		private ImageView flagImage;
		private ImageView handleImage;
		private Language language;

		ViewHolder(View view) {
			super(view);
			this.view = view;
			languageName = view.findViewById(R.id.languageLabel);
			flagImage = view.findViewById(R.id.flagImageView);
			handleImage = view.findViewById(R.id.handleImage);
			handleImage.setOnTouchListener((v, event) -> {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dragListener.onStartDrag(ViewHolder.this);
				}
				return false;
			});
			this.view.setOnClickListener(v -> languageSubject.onNext(language));
		}

		void bindView(Language language, int position) {
			this.language = language;
			if (position == 0)
				languageName.setText(String.format(Locale.getDefault(), view.getResources().getString(R.string.base), language.getLongName()));
			else
				languageName.setText(String.format(Locale.getDefault(), view.getResources().getString(R.string.target), language.getLongName(), position));
			flagImage.setImageResource(language.getLanguageType().getDrawable());
		}
	}
}
