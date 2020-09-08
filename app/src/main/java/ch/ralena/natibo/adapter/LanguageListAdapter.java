package ch.ralena.natibo.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.ralena.natibo.R;
import ch.ralena.natibo.object.Language;
import io.reactivex.subjects.PublishSubject;

public class LanguageListAdapter extends RecyclerView.Adapter<LanguageListAdapter.ViewHolder> {

	PublishSubject<Language> languageSubject = PublishSubject.create();

	public PublishSubject<Language> asObservable() {
		return languageSubject;
	}

	private ArrayList<Language> languages;

	public LanguageListAdapter(ArrayList<Language> languages) {
		this.languages = languages;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language_list, parent, false);
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
		private TextView numPacks;
		private TextView numSentences;
		private ImageView flagImage;
		private Language language;

		ViewHolder(View view) {
			super(view);
			this.view = view;
			languageName = view.findViewById(R.id.languageLabel);
			numPacks = view.findViewById(R.id.numPacksLabel);
			numSentences = view.findViewById(R.id.numSentencesLabel);
			flagImage = view.findViewById(R.id.flagImageView);
			this.view.setOnClickListener(v -> languageSubject.onNext(language));
		}

		void bindView(Language language) {
			this.language = language;
			languageName.setText(language.getLongName());
			numPacks.setText("" + language.getPacks().size());
			numSentences.setText("" + language.getSentenceCount());
			flagImage.setImageResource(language.getLanguageType().getDrawable());
		}
	}
}
