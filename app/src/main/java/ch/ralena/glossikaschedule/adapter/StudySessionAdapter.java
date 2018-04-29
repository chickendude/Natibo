package ch.ralena.glossikaschedule.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.object.Sentence;
import ch.ralena.glossikaschedule.object.SentencePair;
import io.reactivex.subjects.PublishSubject;
import io.realm.RealmList;

public class StudySessionAdapter extends RecyclerView.Adapter<StudySessionAdapter.ViewHolder> {

	PublishSubject<Sentence> languageSubject = PublishSubject.create();

	public PublishSubject<Sentence> asObservable() {
		return languageSubject;
	}

	private RealmList<SentencePair> sentences;
	private String baseLanguageId;
	private String targetLanguageId;

	public StudySessionAdapter(String baseLanguageId, String targetLanguageId, RealmList<SentencePair> sentences) {
		this.baseLanguageId = baseLanguageId;
		this.targetLanguageId = targetLanguageId;
		this.sentences = sentences;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_study_session, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if (position < getItemCount())
			holder.bindView(sentences.get(position));
	}

	@Override
	public int getItemCount() {
		return sentences.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private TextView index;
		private TextView baseLanguageCode;
		private TextView baseSentenceText;
		private TextView baseAlternateSentence;
		private LinearLayout baseAlternateSentenceLayout;
		private TextView baseRomanization;
		private LinearLayout baseRomanizationLayout;
		private TextView baseIpa;
		private LinearLayout baseIpaLayout;
		private TextView targetLanguageCode;
		private TextView targetSentenceText;
		private TextView targetAlternateSentence;
		private LinearLayout targetAlternateSentenceLayout;
		private TextView targetRomanization;
		private LinearLayout targetRomanizationLayout;
		private TextView targetIpa;
		private LinearLayout targetIpaLayout;

		ViewHolder(View view) {
			super(view);
			index = view.findViewById(R.id.indexLabel);
			// base
			baseLanguageCode = view.findViewById(R.id.baseLanguageCodeLabel);
			baseLanguageCode.setText(baseLanguageId);
			baseSentenceText = view.findViewById(R.id.baseSentenceLabel);
			baseAlternateSentence = view.findViewById(R.id.baseAlternateSentenceLabel);
			baseAlternateSentenceLayout = view.findViewById(R.id.baseAlternateSentenceLayout);
			baseRomanization = view.findViewById(R.id.baseRomanizationLabel);
			baseRomanizationLayout = view.findViewById(R.id.baseRomanizationLayout);
			baseIpa = view.findViewById(R.id.baseIpaLabel);
			baseIpaLayout = view.findViewById(R.id.baseIpaLayout);
			// target
			targetLanguageCode = view.findViewById(R.id.targetLanguageCodeLabel);
			targetLanguageCode.setText(targetLanguageId);
			targetSentenceText = view.findViewById(R.id.targetSentenceLabel);
			targetAlternateSentence = view.findViewById(R.id.targetAlternateSentenceLabel);
			targetAlternateSentenceLayout = view.findViewById(R.id.targetAlternateSentenceLayout);
			targetRomanization = view.findViewById(R.id.targetRomanizationLabel);
			targetRomanizationLayout = view.findViewById(R.id.targetRomanizationLayout);
			targetIpa = view.findViewById(R.id.targetIpaLabel);
			targetIpaLayout = view.findViewById(R.id.targetIpaLayout);
		}

		void bindView(SentencePair sentencePair) {
			Sentence targetSentence = sentencePair.getTargetSentence();
			Sentence baseSentence = sentencePair.getBaseSentence();
			index.setText("" + targetSentence.getIndex());
			// base
			baseSentenceText.setText(baseSentence.getText());
			if (targetSentence.getAlternate() != null) {
				baseAlternateSentenceLayout.setVisibility(View.VISIBLE);
				baseAlternateSentence.setText(baseSentence.getAlternate());
			} else {
				baseAlternateSentenceLayout.setVisibility(View.GONE);
			}
			if (baseSentence.getRomanization() != null) {
				baseRomanizationLayout.setVisibility(View.VISIBLE);
				baseRomanization.setText(baseSentence.getRomanization());
			} else {
				baseRomanizationLayout.setVisibility(View.GONE);
			}
			if (baseSentence.getIpa() != null) {
				baseIpaLayout.setVisibility(View.VISIBLE);
				baseIpa.setText(baseSentence.getIpa());
			} else {
				baseIpaLayout.setVisibility(View.GONE);
			}
			// target
			targetSentenceText.setText(targetSentence.getText());
			if (targetSentence.getAlternate() != null) {
				targetAlternateSentenceLayout.setVisibility(View.VISIBLE);
				targetAlternateSentence.setText(targetSentence.getAlternate());
			} else {
				targetAlternateSentenceLayout.setVisibility(View.GONE);
			}
			if (targetSentence.getRomanization() != null) {
				targetRomanizationLayout.setVisibility(View.VISIBLE);
				targetRomanization.setText(targetSentence.getRomanization());
			} else {
				targetRomanizationLayout.setVisibility(View.GONE);
			}
			if (targetSentence.getIpa() != null) {
				targetIpaLayout.setVisibility(View.VISIBLE);
				targetIpa.setText(targetSentence.getIpa());
			} else {
				targetIpaLayout.setVisibility(View.GONE);
			}
		}
	}
}
