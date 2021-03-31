package ch.ralena.natibo.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ch.ralena.natibo.R;
import ch.ralena.natibo.data.room.object.Language;
import ch.ralena.natibo.data.room.object.Sentence;
import io.reactivex.subjects.PublishSubject;
import io.realm.RealmList;

public class StudySessionSentenceAdapter extends RecyclerView.Adapter<StudySessionSentenceAdapter.ViewHolder> {

	private PublishSubject<Sentence> languageSubject = PublishSubject.create();

	public PublishSubject<Sentence> asObservable() {
		return languageSubject;
	}

	private RealmList<Sentence> sentences;
	private RealmList<Language> languages;

	public StudySessionSentenceAdapter(RealmList<Language> languages, RealmList<Sentence> sentences) {
		this.sentences = sentences;
		this.languages = languages;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_study_session_sentence, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if (position < getItemCount())
			holder.bindView(sentences.get(position), languages.get(position), position == sentences.size() - 1);
	}

	@Override
	public int getItemCount() {
		return sentences.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private TextView languageCode;
		private TextView sentenceText;
		private TextView alternateSentence;
		private LinearLayout alternateSentenceLayout;
		private TextView romanization;
		private LinearLayout romanizationLayout;
		private TextView ipa;
		private LinearLayout ipaLayout;
		private View divider;

		ViewHolder(View view) {
			super(view);
			// base
			languageCode = view.findViewById(R.id.languageCodeLabel);
			sentenceText = view.findViewById(R.id.sentenceLabel);
			alternateSentence = view.findViewById(R.id.alternateSentenceLabel);
			alternateSentenceLayout = view.findViewById(R.id.alternateSentenceLayout);
			romanization = view.findViewById(R.id.romanizationLabel);
			romanizationLayout = view.findViewById(R.id.romanizationLayout);
			ipa = view.findViewById(R.id.ipaLabel);
			ipaLayout = view.findViewById(R.id.ipaLayout);
			divider = view.findViewById(R.id.divider);
		}

		void bindView(Sentence sentence, Language language, boolean isLast) {
			// base
			sentenceText.setText(sentence.getText());
			languageCode.setText(language.getLanguageId());

			if (sentence.getAlternate() != null) {
				alternateSentenceLayout.setVisibility(View.VISIBLE);
				alternateSentence.setText(sentence.getAlternate());
			} else {
				alternateSentenceLayout.setVisibility(View.GONE);
			}
			if (sentence.getRomanization() != null) {
				romanizationLayout.setVisibility(View.VISIBLE);
				romanization.setText(sentence.getRomanization());
			} else {
				romanizationLayout.setVisibility(View.GONE);
			}
			if (sentence.getIpa() != null) {
				ipaLayout.setVisibility(View.VISIBLE);
				ipa.setText(sentence.getIpa());
			} else {
				ipaLayout.setVisibility(View.GONE);
			}
			divider.setVisibility(isLast ? View.GONE : View.VISIBLE);
		}
	}
}
