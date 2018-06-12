package ch.ralena.natibo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.ralena.natibo.R;
import ch.ralena.natibo.object.Language;
import io.reactivex.subjects.PublishSubject;

public class CourseSelectedLanguagesAdapter extends RecyclerView.Adapter<CourseSelectedLanguagesAdapter.ViewHolder> {

	PublishSubject<Language> languageSubject = PublishSubject.create();

	public PublishSubject<Language> asObservable() {
		return languageSubject;
	}

	private ArrayList<Language> languages;

	public CourseSelectedLanguagesAdapter(ArrayList<Language> languages) {
		this.languages = languages;
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
		if (position < getItemCount())
			holder.bindView(languages.get(position));
	}

	@Override
	public int getItemCount() {
		return languages.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView languageName;
		private ImageView flagImage;
		private Language language;

		ViewHolder(View view) {
			super(view);
			this.view = view;
			languageName = view.findViewById(R.id.languageLabel);
			flagImage = view.findViewById(R.id.flagImageView);
			this.view.setOnClickListener(v -> languageSubject.onNext(language));
		}

		void bindView(Language language) {
			this.language = language;
			languageName.setText(language.getLongName());
			flagImage.setImageResource(language.getLanguageType().getDrawable());
		}
	}
}
