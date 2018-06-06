package ch.ralena.natibo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ch.ralena.natibo.R;
import ch.ralena.natibo.object.Sentence;
import io.reactivex.subjects.PublishSubject;
import io.realm.RealmList;

public class PickSentenceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int TYPE_SENTENCE = 0;
	private static final int TYPE_MARKER = 1;

	PublishSubject<Sentence> languageSubject = PublishSubject.create();

	public PublishSubject<Sentence> asObservable() {
		return languageSubject;
	}

	private RealmList<Sentence> sentences;
	private String language;

	public PickSentenceAdapter(String languageId, RealmList<Sentence> sentences) {
		this.language = languageId;
		this.sentences = sentences;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		if (viewType == TYPE_MARKER) {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_marker, parent, false);
			return new DivideViewHolder(view);
		} else {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sentence_list, parent, false);
			return new ViewHolder(view);
		}
	}

	@Override
	public int getItemViewType(int position) {
		return isBookMarker(position) ? TYPE_MARKER : TYPE_SENTENCE;
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (position < getItemCount()) {
			if (isBookMarker(position))
				((DivideViewHolder)holder).bindView(sentences.get(position));
			else
				((ViewHolder)holder).bindView(sentences.get(position));
		}
	}

	@Override
	public int getItemCount() {
		return sentences.size();
	}

	private boolean isBookMarker(int position) {
		return sentences.get(position).getIndex() == -1;
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView index;
		private TextView languageCode;
		private TextView sentenceText;
		private TextView alternateSentence;
		private LinearLayout alternateSentenceLayout;
		private TextView romanization;
		private LinearLayout romanizationLayout;
		private TextView ipa;
		private LinearLayout ipaLayout;
		private Sentence sentence;

		ViewHolder(View view) {
			super(view);
			this.view = view;
			index = view.findViewById(R.id.indexLabel);
			languageCode = view.findViewById(R.id.languageCodeLabel);
			languageCode.setText(language);
			sentenceText = view.findViewById(R.id.sentenceLabel);
			alternateSentence = view.findViewById(R.id.alternateSentenceLabel);
			alternateSentenceLayout = view.findViewById(R.id.alternateSentenceLayout);
			romanization = view.findViewById(R.id.romanizationLabel);
			romanizationLayout = view.findViewById(R.id.romanizationLayout);
			ipa = view.findViewById(R.id.ipaLabel);
			ipaLayout = view.findViewById(R.id.ipaLayout);
			this.view.setOnClickListener(v -> languageSubject.onNext(sentence));
		}

		void bindView(Sentence sentence) {
			this.sentence = sentence;
			index.setText("" + sentence.getIndex());
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
		}
	}

	class DivideViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView book;

		DivideViewHolder(View view) {
			super(view);
			this.view = view;
			book = view.findViewById(R.id.bookLabel);
		}

		void bindView(Sentence sentence) {
			book.setText("- " + sentence.getText() + " -");
		}
	}

}
