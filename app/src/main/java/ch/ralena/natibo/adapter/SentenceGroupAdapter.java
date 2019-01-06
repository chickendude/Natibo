package ch.ralena.natibo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ch.ralena.natibo.R;
import ch.ralena.natibo.object.Language;
import ch.ralena.natibo.object.Sentence;
import ch.ralena.natibo.object.SentenceGroup;
import io.reactivex.subjects.PublishSubject;
import io.realm.RealmList;


public class SentenceGroupAdapter extends RecyclerView.Adapter<SentenceGroupAdapter.ViewHolder> {

	PublishSubject<Sentence> languageSubject = PublishSubject.create();

	public PublishSubject<Sentence> asObservable() {
		return languageSubject;
	}

	private RealmList<Sentence> sentences;
	private RealmList<Language> languages;

	public SentenceGroupAdapter() {
		sentences = new RealmList<>();
		languages = new RealmList<>();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sentence_group, parent, false);
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

	public void updateSentencGroup(SentenceGroup sentenceGroup) {
		sentences = sentenceGroup.getSentences();
		languages = sentenceGroup.getLanguages();
		notifyDataSetChanged();
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
			languageCode.setText(language.getLanguageId());
			sentenceText.setText(sentence.getText());
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
